package service.Interactions;

public abstract class Interaction {

  private final long interactionId;
  private final long sourceId;

  public Interaction(long interactionId, long sourceId) {
    this.interactionId = interactionId;
    this.sourceId = interactionId;
  }

  public long getInteractionId() {
    return interactionId;
  }

  public long getSourceId() {
    return sourceId;
  }
}
