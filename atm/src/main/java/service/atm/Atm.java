package service.atm;

import java.util.HashMap;
import java.util.UUID;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import service.core.Status;
import service.message.AddressBookRequest;
import service.message.AddressBookResponse;
import service.message.TransactionRequest;
import service.message.TransactionResponse;
import service.message.TransactionType;
import service.message.ValidationRequest;
import service.message.ValidationResponse;

public class Atm {

  private static long COUNT = 0;
  private long atmId;
  private double balance;
  private String validationToken;

  public String validate(long accountId, int pinNumber) {
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<ValidationRequest> validationHttpRequest = new HttpEntity<>(
        new ValidationRequest(UUID.randomUUID().toString(), accountId, pinNumber));
    ValidationResponse validationResponse =
        restTemplate.postForObject("http://localhost:8085/validation",
            validationHttpRequest, ValidationResponse.class);
    if (validationResponse == null) {
      return "The bank was unable to process your validation";
    }
    System.out.println("Status: " + validationResponse.getStatus());
    System.out.println(validationResponse.getMessage());
    if (validationResponse.getStatus() != Status.SUCCESS) {
      return "Validation was unsuccessful";
    }
    validationToken = validationResponse.getValidationToken();
    return "Validation has completed successfully";
  }

  public String transact(long accountId, TransactionType transactionType, int amount) {

    if (amount <= 0) {
      return "Amount must be a positive number";
    } else if (transactionType == TransactionType.WITHDRAW && amount > balance) {
      return "ATM currently has insufficient funds for this withdrawal";
    } else if (validationToken == null) {
      return "Something went wrong - Your account has not been properly validated yet";
    }

    // transactionId is a combination of the unique atmId and an incrementing number
    String transactionId = atmId + Long.toString(COUNT++);
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<TransactionRequest> transactionHttpRequest = new HttpEntity<>(
        new TransactionRequest(transactionId, accountId, transactionType, amount, validationToken));
    validationToken = null;
    TransactionResponse transactionResponse =
        restTemplate.postForObject("http://localhost:8085/transaction",
            transactionHttpRequest, TransactionResponse.class);
    if (transactionResponse == null) {
      return "The bank was unable to process this transaction";
    }
    System.out.println("Status: " + transactionResponse.getStatus());
    System.out.println(transactionResponse.getMessage());
    if (transactionResponse.getStatus() != Status.SUCCESS) {
      return "Transaction was unsuccessful";
    }
    if (transactionType == TransactionType.DEPOSIT) {
      deposit(amount);
    }
    if (transactionType == TransactionType.WITHDRAW) {
      withdraw(amount);
    }
    return "Transaction has been processed successfully";
  }

  public Atm(long atmId, double balance) {
    this.atmId = atmId;
    this.balance = balance;
  }

  public static void main(String[] args) {
    Atm atm = new Atm(123, 10000);
    System.out.println(atm.validate(1, 1000));
    System.out.println(atm.validate(1, 5555));

    try {
      ConnectionFactory factory = new ActiveMQConnectionFactory("failover://tcp://localhost:61616");
      Connection connection = factory.createConnection();
      connection.setClientID("ATM:" + UUID.randomUUID().toString());
      Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

      Queue addressBookQueue = session.createQueue("addressBookRequest");
      MessageProducer addressBookProducer = session.createProducer(addressBookQueue);

      Topic addressBookTopic = session.createTopic("fullAddressBook");
      MessageConsumer addressBookConsumer = session.createConsumer(addressBookTopic);

      connection.start();

      new Thread(() -> {
        try {
          while (true) {
            Message message = addressBookConsumer.receive();
            if (message instanceof ObjectMessage) {
              Object content = ((ObjectMessage) message).getObject();
              if (content instanceof AddressBookResponse) {
                AddressBookResponse data = (AddressBookResponse) content;
                HashMap newAddressBook = data.getAddressBook();
              }
            }
            message.acknowledge();
          }
        } catch (JMSException e) {
          e.printStackTrace();
        }
      }).start();

      do {
        Thread.sleep(2000);
        Message newRequest = session.createObjectMessage(new AddressBookRequest());
        addressBookProducer.send(newRequest);
      } while (true);

    } catch (JMSException | InterruptedException e) {
      e.printStackTrace();
    }

  }

  private void deposit(double amount) {
    balance += amount;
  }

  private void withdraw(double amount) {
    balance -= amount;
  }

  public long getAtmId() {
    return atmId;
  }

  public void setAtmId(long atmId) {
    this.atmId = atmId;
  }

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }
}
