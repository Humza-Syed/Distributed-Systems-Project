package service.atm;

public class Atm {

  static final int TIMEOUT = 5000;
  private long atmId;
  private double balance;

  public Atm() {
  }

  public void lodge(double amount) {
    balance += amount;
  }

  public void withdraw(double amount) {
    balance -= amount;
  }

  public long getAtmId() {
    return atmId;
  }

  public void setAtmId(long atmId) {
    this.atmId = atmId;
  }

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }
}
