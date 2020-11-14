package service.bank;

import java.util.HashMap;
import java.util.Map;
import service.core.Account;

public class bank {

  private long bankID;
  private String bankName;
  private Map<Long, Account> accounts = new HashMap<Long, Account>();

  public bank() {
  }

  public bank(long bankID, String bankName,
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

  public void setAccounts(Map<Long, Account> accounts) {
    this.accounts = accounts;
  }

  public void addAccount(Account newAccount) {
    this.accounts.put(newAccount.getAccountId(), newAccount);
  }

  public void dropAccount(Long accountID) {
    this.accounts.remove(accountID);
  }

}
