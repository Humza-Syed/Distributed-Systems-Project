package service.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class BankAccount {

  @Id
  @Column
  private long accountId;
  @Column
  private long bankId;
  @Column
  private int pinNumber;
  @Column
  private String accHolderName;
  @Column
  private double balance;

  public BankAccount() {
  }

  public BankAccount(long bankId, long accountId, int pinNumber, String accHolderName,
      double balance) {
    this.bankId = bankId;
    this.accountId = accountId;
    this.pinNumber = pinNumber;
    this.accHolderName = accHolderName;
    this.balance = balance;
  }

  public long getBankId() {
    return bankId;
  }

  public void setBankId(long bankId) {
    this.bankId = bankId;
  }

  public long getAccountId() {
    return accountId;
  }

  public void setAccountId(long accountId) {
    this.accountId = accountId;
  }

  public int getPinNumber() {
    return pinNumber;
  }

  public void setPinNumber(int pinNumber) {
    this.pinNumber = pinNumber;
  }

  public String getAccHolderName() {
    return accHolderName;
  }

  public void setAccHolderName(String accHolderName) {
    this.accHolderName = accHolderName;
  }

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }
}
