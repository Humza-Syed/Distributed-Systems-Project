package service.message;

import java.io.Serializable;

public class BookChange implements Serializable {

  private final AddressBookChange stateChange;
  private final BankIndexElement bankIndexElement;

  public BookChange(AddressBookChange stateChange, BankIndexElement bankIndexElement) {
    this.stateChange = stateChange;
    this.bankIndexElement = bankIndexElement;
  }

  public AddressBookChange getStateChange() {
    return stateChange;
  }

  public BankIndexElement getBankIndexElement() {
    return bankIndexElement;
  }
}
