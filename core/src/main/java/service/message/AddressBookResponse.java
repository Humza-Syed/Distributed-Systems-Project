package service.message;

import java.io.Serializable;
import java.util.HashMap;

public class AddressBookResponse implements Serializable {

  private HashMap addressBook;

  public AddressBookResponse() {

  }

  public AddressBookResponse(HashMap addressBook) {
    this.addressBook = addressBook;
  }

  public HashMap getAddressBook() {
    return addressBook;
  }

  public void setAddressBook(HashMap addressBook) {
    this.addressBook = addressBook;
  }
}
