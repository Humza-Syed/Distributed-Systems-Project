package bankmessage;

import service.message.TransactionResponse;

public class BankTransactionResponse {

  private final String internalToken;
  private final TransactionResponse transactionResponse;


  public BankTransactionResponse(String internalToken,
      TransactionResponse transactionResponse) {
    this.internalToken = internalToken;
    this.transactionResponse = transactionResponse;
  }

  public String getInternalToken() {
    return internalToken;
  }

  public TransactionResponse getTransactionResponse() {
    return transactionResponse;
  }
}
