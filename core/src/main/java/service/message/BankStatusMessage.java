package service.message;

import java.io.Serializable;

public class BankStatusMessage extends Message implements Serializable {

  private final String url;

  public BankStatusMessage(String id, String url) {
    super(id);
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
