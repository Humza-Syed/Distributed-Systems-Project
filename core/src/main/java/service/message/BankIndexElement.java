package service.message;

public class BankIndexElement {
  private final String bankId;
  private String url;

  public BankIndexElement(String bankId, String url) {
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
