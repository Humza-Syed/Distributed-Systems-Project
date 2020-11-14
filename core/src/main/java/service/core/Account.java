package service.core;

import java.io.Serializable;
import java.util.List;

public class Account implements Serializable {

  private long accountId;
  private int pinNumber;
  private String accHolderName;
  private List<String> additionalAccountUsers;
  private double balance;

  public Account() {
  }

  public Account(long accountId, int pinNumber, String accHolderName,
      List<String> additionalAccountUsers, double balance) {
    this.accountId = accountId;
    this.pinNumber = pinNumber;
    this.accHolderName = accHolderName;
    this.additionalAccountUsers = additionalAccountUsers;
    this.balance = balance;
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

  public List<String> getAdditionalAccountUsers() {
    return additionalAccountUsers;
  }

  public void setAdditionalAccountUsers(List<String> additionalAccountUsers) {
    this.additionalAccountUsers = additionalAccountUsers;
  }

  public void addAdditionalAccountUsers(String newUser) {
    this.additionalAccountUsers.add(newUser);
  }

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }
}
