package service.message;

import service.core.Status;

public class TransactionResponse extends Message{

  Status status;
  String message;

  public TransactionResponse(String messageId, Status status, String message) {
    super(messageId);
    this.status = status;
    this.message = message;
  }

  public Status getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }


}
