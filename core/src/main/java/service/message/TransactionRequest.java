package service.message;

import java.io.Serializable;

public class TransactionRequest extends Message {

  private long accountId;
  private TransactionType transactionType;
  private double value;
  private String validationToken;

  public TransactionRequest(String messageId, long accountId, TransactionType transactionType,
      double value, String validationToken) {
    super(messageId);
    this.accountId = accountId;
    this.transactionType = transactionType;
    this.value = value;
    this.validationToken = validationToken;
  }

  public TransactionRequest() { }

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
