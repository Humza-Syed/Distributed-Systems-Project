package service.message;

import java.io.Serializable;

public class HeartbeatRequest implements Serializable {

  private String heartbeatId;

  public HeartbeatRequest(String heartbeatId) { this.heartbeatId = heartbeatId; }

  public String getHeartbeatId() {
    return heartbeatId;
  }
}
