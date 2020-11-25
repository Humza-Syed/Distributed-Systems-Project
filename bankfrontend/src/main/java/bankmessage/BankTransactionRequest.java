package bankmessage;

import service.message.TransactionRequest;

public class BankTransactionRequest {

  private final String internalToken;
  private final TransactionRequest transactionRequest;

  public BankTransactionRequest(String internalToken,
      TransactionRequest transactionRequest) {
    this.internalToken = internalToken;
    this.transactionRequest = transactionRequest;
  }

  public String getInternalToken() {
    return internalToken;
  }

  public TransactionRequest getTransactionRequest() {
    return transactionRequest;
  }
}
