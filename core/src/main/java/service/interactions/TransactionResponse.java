package service.interactions;

public class TransactionResponse {

  long transactionId;
  Status status;
  String message;

  public TransactionResponse(long transactionId, Status status, String message) {
    this.transactionId = transactionId;
    this.status = status;
    this.message = message;
  }

  public long getTransactionId() {
    return transactionId;
  }

  public Status getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}