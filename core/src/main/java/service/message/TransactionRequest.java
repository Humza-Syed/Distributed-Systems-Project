package service.message;

public class TransactionRequest {

  private final long transactionId;
  private final long accountId;
  private final TransactionType transactionType;
  private final double value;
  private final String validationToken;

  public TransactionRequest(long transactionId, long accountId, TransactionType transactionType, double value, String validationToken) {
    this.transactionId = transactionId;
    this.accountId = accountId;
    this.transactionType = transactionType;
    this.value = value;
    this.validationToken = validationToken;
  }

  public long getTransactionId() {
    return transactionId;
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
