package service.message;

import service.core.Status;

public class ValidationResponse extends Message {

  private String validationToken;
  private final Status status;
  private final String message;

  public ValidationResponse(String messageId, String validationToken, Status status, String message) {
    super(messageId);
    this.validationToken = validationToken;
    this.status = status;
    this.message = message;
  }

  public String getValidationToken() {
    return validationToken;
  }

  public Status getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public void setValidationToken(String validationToken) {
    this.validationToken = validationToken;
  }
}
