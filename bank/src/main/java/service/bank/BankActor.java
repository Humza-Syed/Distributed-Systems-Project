package service.bank;

import akka.actor.AbstractActor;
import service.interactions.TransactionRequest;

public class BankActor extends AbstractActor {

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(TransactionRequest.class, msg -> {
          //TODO Actual implementation when updated TransactionRequest/Response are merged
        }).build();
    //TODO ValidationRequest and respond with ValidationResponse (not available in this branch)
  }
}
