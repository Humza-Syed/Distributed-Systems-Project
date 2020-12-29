package service.message;

import java.io.Serializable;

public class LowAtmBalanceRequest implements Serializable {
  private final String atmId;

  public LowAtmBalanceRequest(String atmId) {
    this.atmId = atmId;
  }

  public String getAtmId() {
    return atmId;
  }
}
