package service.message;

import java.io.Serializable;

public class BankStatusMessage implements Serializable {

  private String bankId;
  private String url;

  public BankStatusMessage() {
  }

  public BankStatusMessage(String id, String url) {
    this.bankId = id;
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public String getBankId() {
    return bankId;
  }
}
