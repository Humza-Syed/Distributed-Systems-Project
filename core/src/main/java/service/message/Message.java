package service.message;

import java.io.Serializable;

public class Message {
  private String messageId;

  public String getMessageId() {
    return messageId;
  }

  public Message(String id) {
    this.messageId = id;
  }

  public Message() {
  }
}
