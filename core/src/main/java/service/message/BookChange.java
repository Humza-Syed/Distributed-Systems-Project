package service.message;

import java.io.Serializable;

public class BookChange implements Serializable {

  private final AddressBookChange stateChange;
  private final BankInfo bankInfo;

  public BookChange(AddressBookChange stateChange, BankInfo bankInfo) {
    this.stateChange = stateChange;
    this.bankInfo = bankInfo;
  }

  public AddressBookChange getStateChange() {
    return stateChange;
  }

  public BankInfo getBankInfo() {
    return bankInfo;
  }
}
