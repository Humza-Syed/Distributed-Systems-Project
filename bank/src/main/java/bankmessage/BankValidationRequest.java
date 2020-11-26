package bankmessage;

import service.message.ValidationRequest;

public class BankValidationRequest {

  private final String internalToken;
  private final ValidationRequest validationRequest;

  public BankValidationRequest(String internalToken,
      ValidationRequest validationRequest) {
    this.internalToken = internalToken;
    this.validationRequest = validationRequest;
  }

  public String getinternalToken() {
    return internalToken;
  }

  public ValidationRequest getValidationRequest() {
    return validationRequest;
  }
}
