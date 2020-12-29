package service.message;

import java.io.Serializable;

public class LowAtmBalanceResponse implements Serializable {
  private double topUpAmount;
  private String atmId;

  public double getTopUpAmount() {
    return topUpAmount;
  }
  public String getAtmId() {return atmId;}

  public LowAtmBalanceResponse(double topUpAmount, String atmId) {
    this.topUpAmount = topUpAmount;
    this.atmId = atmId;
  }
}
