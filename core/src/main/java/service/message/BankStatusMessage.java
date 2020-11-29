package service.message;

import java.io.Serializable;

public class BankStatusMessage implements Serializable {

  private String messageId;
  private String url;

  public BankStatusMessage() {
  }

  public BankStatusMessage(String id, String url) {
    this.messageId = id;
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public String getMessageId() {
    return messageId;
  }
}
