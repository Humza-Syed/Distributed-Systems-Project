package service.atmmanager;

import java.util.HashMap;
import java.util.Map;
import service.atm.Atm;

public class AtmManager {

  private Map<Long, Atm> atms;

  public AtmManager(Map<Long, Atm> atms) {
    this.atms = atms;
  }

  public AtmManager() {
    atms = new HashMap<Long, Atm>();
  }

  public Map<Long, Atm> getAtms() {
    return atms;
  }

  public void setAtms(Map<Long, Atm> atms) {
    this.atms = atms;
  }
}
