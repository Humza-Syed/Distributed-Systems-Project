package service.bank;

import java.util.HashMap;
import java.util.Map;
import service.core.Account;

public class Bank {

  private long bankID;
  private String bankName;
  private Map<Long, Account> accounts = new HashMap<Long, Account>();

  public Bank() {
  }

  public Bank(long bankID, String bankName,
      Map<Long, Account> accounts) {
    this.bankID = bankID;
    this.bankName = bankName;
    this.accounts = accounts;
  }

  public long getBankID() {
    return bankID;
  }

  public void setBankID(long bankID) {
    this.bankID = bankID;
  }

  public String getBankName() {
    return bankName;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  public Map<Long, Account> getAccounts() {
    return accounts;
  }

  public Account getAccount(long accountId) {
    return accounts.get(accountId);
  }

  public void setAccounts(Map<Long, Account> accounts) {
    this.accounts = accounts;
  }

  public void addAccount(Account newAccount) {
    this.accounts.put(newAccount.getAccountId(), newAccount);
  }

  public void dropAccount(Long accountID) {
    this.accounts.remove(accountID);
  }

  private boolean validCredentialsCheck(Long accountID, Long pin) {
    return this.accounts.containsKey(accountID)
        && this.accounts.get(accountID).getPinNumber() == pin;
  }

  public void displayAccount(Long accountID, Long pin) {
    System.out.println(
        "|=================================================================================================================|");
    System.out.println(
        "|                                     |                                     |                                     |");
    if (validCredentialsCheck(accountID, pin)) {
      Account account = this.accounts.get(accountID);
      System.out.println(
          "| ID: " + String.format("%1$-32s", account.getAccountId()) +
              "| Name: " + String.format("%1$-29s", account.getAccHolderName()) +
              " | Balance: " + String.format("%1$-26s", account.getBalance()) + " |");
    } else {
      System.out.println("INVALID CREDENTIALS");
    }
    System.out.println(
        "|                                     |                                     |                                     |");
    System.out.println(
        "|=================================================================================================================|");
  }

  public static Map<Long, Account> importAccounts() {
    Map<Long, Account> defaultAccounts = new HashMap();
    defaultAccounts.put(01L, new Account(01L, 1000, "testAcc1", null, 1000));
    defaultAccounts.put(02L, new Account(02L, 2000, "testAcc2", null, 2000));
    defaultAccounts.put(03L, new Account(03L, 3000, "testAcc3", null, 3000));
    defaultAccounts.put(04L, new Account(04L, 4000, "testAcc4", null, 4000));
    defaultAccounts.put(05L, new Account(05L, 5000, "testAcc5", null, 5000));
    return defaultAccounts;
  }

  public static void main(String[] args) {

    Bank newBank = new Bank(0, "testBank", importAccounts());
    System.out.println(newBank);
    System.out.println(newBank.bankID);
    System.out.println(newBank.getAccounts());
    newBank.displayAccount(01L, 1000L);
  }

}
