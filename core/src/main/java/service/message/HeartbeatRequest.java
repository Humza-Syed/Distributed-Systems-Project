package service.message;

public class HeartbeatRequest {

  private long id;

  public long getId() {
    return id;
  }

  public HeartbeatRequest(long id) {
    this.id = id;
  }
}
