package service.bank;

import akka.actor.AbstractActor;
import bankmessage.BankTransactionResponse;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import service.core.Status;
import service.message.TransactionRequest;
import service.message.TransactionResponse;
import service.message.ValidationRequest;
import service.message.ValidationResponse;
import service.model.BankAccount;

public class BankActor extends AbstractActor {

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(ValidationRequest.class, msg -> {
          getContext().getSender().tell(processValidationRequest(msg), getSelf());
        })
        //Incoming TransactionRequests forwarded from the BankFrontEnd
        .match(TransactionRequest.class, msg -> {
          getContext().getSender().tell(processTransactionRequest(msg), getSelf());
        }).build();
  }

  private ValidationResponse processValidationRequest(ValidationRequest validationRequest) {
    EntityManagerFactory entityManagerFactory = Persistence
        .createEntityManagerFactory("account_database_access");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    BankAccount bankAccount = entityManager
        .find(BankAccount.class, validationRequest.getAccountId());

    if (bankAccount != null && bankAccount.getPinNumber() == validationRequest.getPinNumber()) {
      return new ValidationResponse(validationRequest.getMessageId(), UUID.randomUUID().toString(),
          Status.SUCCESS, "Account is validated");
    }

    entityManager.close();
    entityManagerFactory.close();

    return new ValidationResponse(validationRequest.getMessageId(),
        null, Status.FAILURE, "Invalid credentials");
  }

  private BankTransactionResponse processTransactionRequest(TransactionRequest transactionRequest) {
    EntityManagerFactory entityManagerFactory = Persistence
        .createEntityManagerFactory("account_database_access");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    BankAccount bankAccount = entityManager.find(BankAccount.class, transactionRequest.getAccountId());

    BankTransactionResponse response;

    switch (transactionRequest.getTransactionType()) {
      case DEPOSIT:
        // Can't deposit a negative or a zero value
        if (transactionRequest.getValue() <= 0) {
          response = new BankTransactionResponse(transactionRequest.getValidationToken(),
              new TransactionResponse(transactionRequest.getMessageId(),
                  Status.FAILURE,
                  "Value cannot be negative"));
          break;
        }

        // Value is deposited.
        entityManager.getTransaction().begin();
        deposit(bankAccount, transactionRequest.getValue());
        entityManager.getTransaction().commit();

        response = new BankTransactionResponse(transactionRequest.getValidationToken(),
            new TransactionResponse(transactionRequest.getMessageId(),
                Status.SUCCESS,
                "Deposit successful. New balance: " + bankAccount.getBalance()));
        break;
      case WITHDRAW:
        // Can't withdraw a negative or a zero value
        if (transactionRequest.getValue() <= 0) {
          response = new BankTransactionResponse(transactionRequest.getValidationToken(),
              new TransactionResponse(transactionRequest.getMessageId(),
                  Status.FAILURE,
                  "Value cannot be negative"));
          break;
        }

        // Cant overdraw the account
        //TODO Add overdraw limit to accounts?
        if (bankAccount.getBalance() - transactionRequest.getValue() >= 0) {
          entityManager.getTransaction().begin();
          withdraw(bankAccount, transactionRequest.getValue());
          entityManager.getTransaction().commit();

          response = new BankTransactionResponse(transactionRequest.getValidationToken(),
              new TransactionResponse(transactionRequest.getMessageId(),
                  Status.SUCCESS,
                  "Withdraw successful. New balance = " + bankAccount.getBalance()));
          break;
        }

        // Account would have been overdrawn and the withdrawal fails
        response = new BankTransactionResponse(transactionRequest.getValidationToken(),
            new TransactionResponse(transactionRequest.getMessageId(),
                Status.FAILURE,
                "Cannot exceed overdraw limit"));
        break;
      default:
        response = new BankTransactionResponse(transactionRequest.getValidationToken(),
            new TransactionResponse(transactionRequest.getMessageId(),
                Status.ERROR,
                "Unknown transaction type"));
    }

    entityManager.close();
    entityManagerFactory.close();
    return response;
  }

  private void deposit(BankAccount bankAccount, double value) {
    bankAccount.setBalance(bankAccount.getBalance() + value);
  }

  private void withdraw(BankAccount bankAccount, double value) {
    bankAccount.setBalance(bankAccount.getBalance() - value);
  }
}
