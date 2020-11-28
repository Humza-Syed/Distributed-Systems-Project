package service.message;

public class TransactionRequest extends Message{

  private final long accountId;
  private final TransactionType transactionType;
  private final double value;
  private final String validationToken;

  public TransactionRequest(String messageId, long accountId, TransactionType transactionType, double value, String validationToken) {
    super(messageId);
    this.accountId = accountId;
    this.transactionType = transactionType;
    this.value = value;
    this.validationToken = validationToken;
  }

  public long getAccountId() {
    return accountId;
  }

  public TransactionType getTransactionType() {
    return transactionType;
  }

  public double getValue() {
    return value;
  }

  public String getValidationToken() {
    return validationToken;
  }
}
