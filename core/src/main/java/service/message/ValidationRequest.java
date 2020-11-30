package service.message;

public class ValidationRequest extends Message{

  private long accountId;
  private int pinNumber;

  public ValidationRequest(String messageId, long accountId, int pinNumber) {
    super(messageId);
    this.accountId = accountId;
    this.pinNumber = pinNumber;
  }

  public ValidationRequest() { }

  public long getAccountId() {
    return accountId;
  }

  public int getPinNumber() {
    return pinNumber;
  }
}
