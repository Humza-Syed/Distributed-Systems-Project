package service.interactions;

public class TransactionRequest extends Interaction {

  private final long bankId;
  private final long accountId;
  private final TransactionType transaction;
  private final double value;

  public TransactionRequest(long interactionId, long sourceId, long bankId, long accountId,
      int accountPin, TransactionType transaction, double value) {
    super(interactionId, sourceId);
    this.bankId = bankId;
    this.accountId = accountId;
    this.transaction = transaction;
    this.value = value;
  }

  public long getBankId() {
    return bankId;
  }

  public long getAccountId() {
    return accountId;
  }

  public TransactionType getTransaction() {
    return transaction;
  }

  public double getValue() {
    return value;
  }
}
