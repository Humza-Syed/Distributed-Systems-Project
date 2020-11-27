package bankmessage;

import service.message.ValidationResponse;

public class BankValidationResponse {

  private final String internalToken;
  private final ValidationResponse validationResponse;

  public BankValidationResponse(String internalToken,
      ValidationResponse validationResponse) {
    this.internalToken = internalToken;
    this.validationResponse = validationResponse;
  }

  public String getinternalToken() {
    return internalToken;
  }

  public ValidationResponse getValidationResponse() {
    return validationResponse;
  }
}
