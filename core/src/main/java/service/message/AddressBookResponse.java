package service.message;

import java.io.Serializable;
import java.util.HashMap;

public class AddressBookResponse implements Serializable {

  private HashMap<String, String> addressBook;

  public AddressBookResponse() {

  }

  public AddressBookResponse(HashMap<String, String> addressBook) {
    this.addressBook = addressBook;
  }

  public HashMap<String, String> getAddressBook() {
    return addressBook;
  }

  public void setAddressBook(HashMap<String, String> addressBook) {
    this.addressBook = addressBook;
  }
}