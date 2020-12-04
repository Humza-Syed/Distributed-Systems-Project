package service.message;

import java.io.Serializable;

public class BankInfo implements Serializable {
  private final String bankId;
  private String url;

  public BankInfo(String bankId, String url) {
    this.bankId = bankId;
    this.url = url;
  }

  public String getBankId() {
    return bankId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
