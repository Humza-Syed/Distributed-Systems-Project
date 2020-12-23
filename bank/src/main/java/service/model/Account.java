package service.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Account {

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
  @Column
  @CreationTimestamp
  private Date created_date;
  @Column
  @CreationTimestamp
  private Date last_updated_date;

  public Account() {
  }

  public Account(long bankId, long accountId, int pinNumber,
      String accHolderName, double balance) {
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

  public Date getCreated_date() {
    return created_date;
  }

  public void setCreated_date(Date created_date) {
    this.created_date = created_date;
  }

  public Date getLast_updated_date() {
    return last_updated_date;
  }

  public void setLast_updated_date(Date last_updated_date) {
    this.last_updated_date = last_updated_date;
  }
}
