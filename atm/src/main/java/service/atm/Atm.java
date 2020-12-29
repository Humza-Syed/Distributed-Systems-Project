package service.atm;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.Queue;
import javax.jms.Message;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import service.core.Status;
import service.message.HeartbeatRequest;
import service.message.HeartbeatResponse;
import service.message.LowAtmBalanceRequest;
import service.message.LowAtmBalanceResponse;
import service.message.AddressBookChange;
import service.message.AddressBookRequest;
import service.message.AddressBookResponse;
import service.message.BookChange;
import service.message.TransactionRequest;
import service.message.TransactionResponse;
import service.message.TransactionType;
import service.message.ValidationRequest;
import service.message.ValidationResponse;

public class Atm {

  public static final String HEARTBEAT_REQUEST_TOPIC_NAME = "atmToAtmManagerMessageTopic";
  public static final String HEARTBEAT_RESPONSE_QUEUE_NAME = "atmToAtmManagerMessageQueue";

  private static long COUNT = 0;
  private String atmId;
  private double balance;
  private String validationToken;
  private String atmUrl;

  private static HashMap<String, String> addressBook = new HashMap<>();

  private Connection connection;
  private Session session;
  private MessageConsumer addressBookConsumer;
  private MessageConsumer addressBookUpdateConsumer;
  private MessageProducer addressBookProducer;
  private MessageConsumer atmManagerMessageConsumer;
  private MessageProducer atmManagerMessageProducer;

  public String validate(long accountId, int pinNumber, String bankId) {
    if(addressBook.get(bankId) == null){
      return "Unknown Bank";
    }
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<ValidationRequest> validationHttpRequest = new HttpEntity<>(
        new ValidationRequest(UUID.randomUUID().toString(), accountId, pinNumber));
    ValidationResponse validationResponse;
    try {
      validationResponse = restTemplate.postForObject(addressBook.get(bankId) + "validation",
          validationHttpRequest, ValidationResponse.class);
    } catch (RestClientException e) {
      System.out.println("Trouble connecting - Retrying...");
      try {
        Thread.sleep(15000);
        validationResponse = restTemplate.postForObject(addressBook.get(bankId) + "validation",
            validationHttpRequest, ValidationResponse.class);
      } catch (InterruptedException | RestClientException e1) {
        try {
          Message newRequest = session.createObjectMessage(new AddressBookRequest());
          addressBookProducer.send(newRequest);
        } catch (JMSException e2) {
          e2.printStackTrace();
        }
        return "Could not connect to bank";
      }
    }
    if (validationResponse == null) {
      return "The bank was unable to process your validation";
    }
    System.out.println("Status: " + validationResponse.getStatus());
    System.out.println(validationResponse.getMessage());
    if (validationResponse.getStatus() != Status.SUCCESS) {
      return validationResponse.getMessage() + " - Validation was unsuccessful";
    }
    validationToken = validationResponse.getValidationToken();

    new Thread(() -> {
      try {
        Thread.sleep(2 * 60 * 1000);
        // so atm's current state (i.e. stored variables) will "time out" after 2 minutes
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      validationToken = null;
    }).start();

    return "Validation has completed successfully";
  }

  public String transact(long accountId, TransactionType transactionType, int amount,
      String bankId) {
    if(addressBook.get(bankId) == null){
      return "Unknown Bank";
    }

    if (amount <= 0) {
      return "Amount must be a positive number";
    } else if (transactionType == TransactionType.WITHDRAW && amount > balance) {
      LowAtmBalanceRequest lowAtmBalanceRequest = new LowAtmBalanceRequest(atmId);
      try {
        atmManagerMessageProducer.send(session.createObjectMessage(lowAtmBalanceRequest));
      } catch(Exception exception) {
        exception.printStackTrace();
      }
      System.out.println("Atm[" + atmId + "] sent a low balance warning to atm manager.");
      return "ATM currently has insufficient funds for this withdrawal";
    } else if (validationToken == null) {
      return "Something went wrong - Your account has not been properly validated yet";
    }

    // transactionId is a combination of the unique atmId and an incrementing number
    String transactionId = atmId + COUNT++;
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<TransactionRequest> transactionHttpRequest = new HttpEntity<>(
        new TransactionRequest(transactionId, accountId, transactionType, amount, validationToken));
    validationToken = null;
    TransactionResponse transactionResponse;
    try {
      transactionResponse = restTemplate.postForObject(addressBook.get(bankId) + "transaction",
          transactionHttpRequest, TransactionResponse.class);
    } catch (RestClientException e) {
      System.out.println("Trouble connecting - Retrying...");
      try {
        Thread.sleep(15000);
        transactionResponse = restTemplate.postForObject(addressBook.get(bankId) + "transaction",
            transactionHttpRequest, TransactionResponse.class);
      } catch (InterruptedException | RestClientException e1) {
        try {
          Message newRequest = session.createObjectMessage(new AddressBookRequest());
          addressBookProducer.send(newRequest);
        } catch (JMSException e2) {
          e2.printStackTrace();
        }
        return "Could not connect to bank";
      }
    }
    if (transactionResponse == null) {
      return "The bank was unable to process this transaction";
    }
    System.out.println("Status: " + transactionResponse.getStatus());
    System.out.println(transactionResponse.getMessage());
    if (transactionResponse.getStatus() != Status.SUCCESS) {
      return transactionResponse.getMessage() + " - Transaction was unsuccessful";
    }
    if (transactionType == TransactionType.DEPOSIT) {
      deposit(amount);
    }
    if (transactionType == TransactionType.WITHDRAW) {
      withdraw(amount);
    }
    return "Transaction has been processed successfully";
  }

  private void initialise() throws JMSException {
    ConnectionFactory factory = new ActiveMQConnectionFactory(atmUrl);
    connection = factory.createConnection();
    connection.setClientID("ATM:" + atmId);
    session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

    Topic atmManagerMessageTopic = session.createTopic(HEARTBEAT_REQUEST_TOPIC_NAME);
    atmManagerMessageConsumer = session.createConsumer(atmManagerMessageTopic);

    Queue atmManagerMessageQueue = session.createQueue(HEARTBEAT_RESPONSE_QUEUE_NAME);
    atmManagerMessageProducer = session.createProducer(atmManagerMessageQueue);

    Queue addressBookQueue = session.createQueue("addressBookRequest");
    addressBookProducer = session.createProducer(addressBookQueue);

    Topic addressBookTopic = session.createTopic("fullAddressBook");
    addressBookConsumer = session.createConsumer(addressBookTopic);

    Topic addressBookUpdateTopic = session.createTopic("addressBookUpdate");
    addressBookUpdateConsumer = session.createConsumer(addressBookUpdateTopic);
  }

  public Atm(String atmId, double balance, String atmUrl) {
    this.atmId = atmId;
    this.balance = balance;
    this.atmUrl = atmUrl;
  }

  public static void main(String[] args) {
    String hostName = (args.length == 0) ? "localhost" : args[0];
    String atmUrl = "failover://tcp://" + hostName + ":61616";
    String uniqueAtmID = UUID.randomUUID().toString();

    Atm atm = new Atm(uniqueAtmID, 10000, atmUrl);
    atm.start();
    // Note: For live testing, bankId argument for validate and transact will need to match a real bank's bankId,
    // so you can temporarily change the value set in BankRestFront.sendStatus()
    // to a String literal instead of a random UUID ,so it's value can be known before compile time

  }

  public void start() {
    try {
      initialise();
      connection.start();
      new Thread(() -> {
        try {
          while (true) {
            Message message = addressBookConsumer.receive();
            if (message instanceof ObjectMessage) {
              Object content = ((ObjectMessage) message).getObject();
              if (content instanceof AddressBookResponse) {
                AddressBookResponse data = (AddressBookResponse) content;
                addressBook = data.getAddressBook();
              }
            }
            message.acknowledge();
          }
        } catch (JMSException e) {
          e.printStackTrace();
        }
      }).start();

      new Thread(() -> {
        try {
          Message newRequest = session.createObjectMessage(new AddressBookRequest());
          do {
            Thread.sleep(2000);
            addressBookProducer.send(newRequest);
          } while (true);

        } catch (JMSException | InterruptedException e) {
          e.printStackTrace();
        }
      }).start();

      new Thread(() -> {
        try {
          while (true) {
            Message message = addressBookUpdateConsumer.receive();
            if (message instanceof ObjectMessage) {
              Object content = ((ObjectMessage) message).getObject();
              if (content instanceof BookChange) {
                BookChange data = (BookChange) content;
                if (data.getStateChange() == AddressBookChange.DROP){
                  addressBook.remove(data.getBankInfo().getBankId());
                } else {
                  addressBook.put(data.getBankInfo().getBankId(), data.getBankInfo().getUrl());
                }
              }
            }
            message.acknowledge();
          }
        } catch (JMSException e) {
          e.printStackTrace();
        }
      }).start();

      while(true) {
        Message message = atmManagerMessageConsumer.receive();
        if (message instanceof ObjectMessage) {
          Object content = ((ObjectMessage) message).getObject();

          if(content instanceof HeartbeatRequest) {
            HeartbeatRequest heartbeatRequest = (HeartbeatRequest) content;
            HeartbeatResponse heartbeatResponse = new HeartbeatResponse(heartbeatRequest.getHeartbeatId(), atmId, atmUrl);
            if(balance < 100) {
              LowAtmBalanceRequest lowAtmBalanceRequest = new LowAtmBalanceRequest(atmId);
              atmManagerMessageProducer.send(session.createObjectMessage(lowAtmBalanceRequest));
            }
            Message response = session.createObjectMessage(heartbeatResponse);
            atmManagerMessageProducer.send(response);

          } else if(content instanceof LowAtmBalanceResponse) {
            LowAtmBalanceResponse lowAtmBalanceResponse = (LowAtmBalanceResponse) content;
            if(lowAtmBalanceResponse.getAtmId().equals(atmId)) balance = balance + lowAtmBalanceResponse.getTopUpAmount();
          }
        }
        message.acknowledge();
      }
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

  private void deposit(double amount) {
    balance += amount;
  }

  private void withdraw(double amount) {
    balance -= amount;
  }

  public Set<String> getKnownBanks() {
    return addressBook.keySet();
  }

  public String getAtmUrl() {
    return atmUrl;
  }

  public String getAtmId() {
    return atmId;
  }

  public void setAtmId(String atmId) {
    this.atmId = atmId;
  }

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }
}