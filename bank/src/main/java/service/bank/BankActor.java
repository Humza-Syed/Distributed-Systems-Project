package service.bank;

import akka.actor.AbstractActor;
import bankmessage.BankTransactionResponse;
import java.util.UUID;
import service.core.Account;
import service.core.Status;
import service.message.TransactionRequest;
import service.message.TransactionResponse;
import service.message.ValidationRequest;
import service.message.ValidationResponse;

public class BankActor extends AbstractActor {

  private static Bank testBank = new Bank(0, "testBank", Bank.importAccounts());

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
    Account clientAccount = testBank.getAccount(validationRequest.getAccountId());
    if (clientAccount != null && clientAccount.getPinNumber() == validationRequest.getPinNumber()) {
      return new ValidationResponse(validationRequest.getMessageId(),
          UUID.randomUUID().toString(), Status.SUCCESS,
          "Account is validated");
    }
    return new ValidationResponse(validationRequest.getMessageId(),
        null, Status.FAILURE, "Invalid credentials");
  }

  private BankTransactionResponse processTransactionRequest(TransactionRequest transactionRequest) {
    Account clientAccount = testBank.getAccount(transactionRequest.getAccountId());

    switch (transactionRequest.getTransactionType()) {
      case DEPOSIT:
        // Can't deposit a negative or a zero value
        if (transactionRequest.getValue() <= 0) {
          return new BankTransactionResponse(transactionRequest.getValidationToken(),
              new TransactionResponse(transactionRequest.getMessageId(),
                  Status.FAILURE,
                  "Value cannot be negative"));
        }

        // Value is deposited.
        clientAccount.deposit(transactionRequest.getValue());
        return new BankTransactionResponse(transactionRequest.getValidationToken(),
            new TransactionResponse(transactionRequest.getMessageId(),
                Status.SUCCESS,
                "Deposit successful. New balance: " + clientAccount.getBalance()));
      case WITHDRAW:
        // Can't withdraw a negative or a zero value
        if (transactionRequest.getValue() <= 0) {
          return new BankTransactionResponse(transactionRequest.getValidationToken(),
              new TransactionResponse(transactionRequest.getMessageId(),
                  Status.FAILURE,
                  "Value cannot be negative"));
        }

        // Cant overdraw the account
        //TODO Add overdraw limit to accounts?
        if (clientAccount.getBalance() - transactionRequest.getValue() > 0) {
          clientAccount.withdraw(transactionRequest.getValue());
          return new BankTransactionResponse(transactionRequest.getValidationToken(),
              new TransactionResponse(transactionRequest.getMessageId(),
                  Status.SUCCESS,
                  "Withdraw successful. New balance = " + clientAccount.getBalance()));
        }

        // Account would have been overdrawn and the withdrawal fails
        return new BankTransactionResponse(transactionRequest.getValidationToken(),
            new TransactionResponse(transactionRequest.getMessageId(),
                Status.FAILURE,
                "Cannot exceed overdraw limit"));
      default:
        return new BankTransactionResponse(transactionRequest.getValidationToken(),
            new TransactionResponse(transactionRequest.getMessageId(),
                Status.ERROR,
                "Unknown transaction type"));
    }
  }
}
