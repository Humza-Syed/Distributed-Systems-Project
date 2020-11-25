package service.message;

public class ValidationRequest {

  private final long accountId;
  private final int pinNumber;

  public ValidationRequest(long accountId, int pinNumber) {
    this.accountId = accountId;
    this.pinNumber = pinNumber;
  }

  public long getAccountId() {
    return accountId;
  }

  public int getPinNumber() {
    return pinNumber;
  }
}
