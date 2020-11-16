package service.interactions;

public abstract class Interaction {

  private final long interactionId;
  private final long sourceId;

  public Interaction(long interactionId, long sourceId) {
    this.interactionId = interactionId;
    this.sourceId = sourceId;
  }

  public long getInteractionId() {
    return interactionId;
  }

  public long getSourceId() {
    return sourceId;
  }
}
