package service.message;

import java.io.Serializable;

public class HeartbeatResponse implements Serializable {

  private String heartbeatId;
  private String atmId;
  private String atmUrl;

  public HeartbeatResponse(String heartbeatId, String atmId, String atmUrl) {
    this.heartbeatId = heartbeatId;
    this.atmId = atmId;
    this.atmUrl = atmUrl;
  }

  public String getAtmUrl() {
    return atmUrl;
  }

  public String getHeartbeatId() {return heartbeatId;}

  public String getAtmId() {return atmId;}
}
