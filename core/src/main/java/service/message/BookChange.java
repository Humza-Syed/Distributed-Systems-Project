package service.message;

public class BookChange {

  private final BookChange stateChange;
  private final BankIndexElement bankIndexElement;

  public BookChange(BookChange stateChange, BankIndexElement bankIndexElement) {
    this.stateChange = stateChange;
    this.bankIndexElement = bankIndexElement;
  }

  public BookChange getStateChange() {
    return stateChange;
  }

  public BankIndexElement getBankIndexElement() {
    return bankIndexElement;
  }
}
