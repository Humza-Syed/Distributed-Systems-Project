package service.bankfrontend;

import static service.bankfrontend.Application.system;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import bankmessage.BankValidationRequest;
import bankmessage.BankValidationResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import scala.concurrent.Await;
import scala.concurrent.Future;
import service.bank.BankActor;
import service.core.ActorStatus;
import service.core.Status;
import service.message.ValidationRequest;
import service.message.ValidationResponse;


@RestController
public class BankRestFront {

  public static final String deniedToken = UUID.randomUUID().toString();
  private static final int responseLimit = 3;
  public static Map<ActorRef, ActorStatus> actors = new HashMap<>();
  private static Map<String, Long> validTokens = new HashMap<>();

  @RequestMapping(value = "/validation", method = RequestMethod.POST)
  public ResponseEntity<ValidationResponse> credentialValidation(
      @RequestBody ValidationRequest request)
      throws URISyntaxException, InterruptedException, TimeoutException {

    String internalToken = UUID.randomUUID().toString();
    BankValidationRequest newInternalRequest = new BankValidationRequest(internalToken, request);

    //TODO investigate that this is correct actor msg implementation
    Timeout t = new Timeout(responseLimit, TimeUnit.SECONDS);
    ActorRef handler = getActor();
    Future<Object> message = Patterns.ask(handler, newInternalRequest, t);

    BankValidationResponse response = (BankValidationResponse) Await.result(message, t.duration());
    actors.put(handler, ActorStatus.AVAILABLE);

    ValidationResponse atmResponse = response.getValidationResponse();
    if (response.getValidationResponse().getStatus() == Status.SUCCESS) {
      validTokens.replace(response.getinternalToken(), request.getAccountId());
    } else {
      atmResponse.setValidationToken(deniedToken);
    }

    String path =
        ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
            + "/validation/"
            + internalToken;

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(new URI(path));
    return new ResponseEntity<>(atmResponse, headers, HttpStatus.CREATED);
  }

  public static int getActorWaitTime() {
    return 0;
  }

  public static ActorRef getActor() throws InterruptedException {
    int time_wait = getActorWaitTime();
    TimeUnit.MILLISECONDS.sleep(time_wait);
    for (Entry<ActorRef, ActorStatus> actorRefActorStatusEntry : actors.entrySet()) {
      if (((Entry) actorRefActorStatusEntry).getValue() == ActorStatus.AVAILABLE) {
        actors.replace(actorRefActorStatusEntry.getKey(), ActorStatus.UNAVAILABLE);
        return (ActorRef) ((Entry) actorRefActorStatusEntry).getKey();
      }
    }
    ActorRef newActor = system
        .actorOf(Props.create(BankActor.class), UUID.randomUUID().toString());
    actors.put(newActor, ActorStatus.AVAILABLE);
    return newActor;
  }
}
