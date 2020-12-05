package service.bankfrontend;

import static service.bankfrontend.Application.system;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import bankmessage.BankTransactionResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
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
import service.message.BankInfo;
import service.message.TransactionRequest;
import service.message.TransactionResponse;
import service.message.ValidationRequest;
import service.message.ValidationResponse;


@RestController
public class BankRestFront {

  private static final int RESPONSE_LIMIT = 3;
  public static Map<ActorRef, ActorStatus> actors = new HashMap<>();
  private static Map<String, Long> validTokens = new HashMap<>();

  @RequestMapping(value = "/validation", method = RequestMethod.POST)
  public ResponseEntity<ValidationResponse> credentialValidation(
      @RequestBody ValidationRequest request)
      throws URISyntaxException, InterruptedException, TimeoutException {

    Timeout t = new Timeout(RESPONSE_LIMIT, TimeUnit.SECONDS);
    ActorRef handler = getActor();
    Future<Object> message = Patterns.ask(handler, request, t);

    ValidationResponse response = (ValidationResponse) Await.result(message, t.duration());
    actors.put(handler, ActorStatus.AVAILABLE);

    if (response.getStatus() == Status.SUCCESS) {
      validTokens.put(response.getValidationToken(), request.getAccountId());
    }

    String atmPath =
        ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
            + "/validation/"
            + response.getMessageId();

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(new URI(atmPath));
    return new ResponseEntity<>(response, headers, HttpStatus.CREATED);
  }

  @RequestMapping(value = "/transaction", method = RequestMethod.POST)
  public ResponseEntity<TransactionResponse> transactionHandling(
      @RequestBody TransactionRequest transactionRequest)
      throws InterruptedException, TimeoutException, URISyntaxException {
    TransactionResponse transactionResponse;

    // throwing away any messages with null tokens
    if (transactionRequest.getValidationToken() == null) {
      transactionResponse = new TransactionResponse(transactionRequest.getMessageId(),
          Status.FAILURE,
          "Failure: Bank received transaction with null token");
    } else if (!validTokens.containsKey(transactionRequest.getValidationToken())) {
      transactionResponse = new TransactionResponse(transactionRequest.getMessageId(),
          Status.FAILURE,
          "Failure: Bank received transaction with a illegal token");
    } else if (validTokens.get(transactionRequest.getValidationToken()) !=
        transactionRequest.getAccountId()) {
      transactionResponse = new TransactionResponse(transactionRequest.getMessageId(),
          Status.FAILURE,
          "Failure: Bank received transaction with mismatched token and id");
    } else { // token has to be legal

      // sending the package asynchronously to a free bank actor,
      // waiting until receiving response, freeing actor again
      Timeout time = new Timeout(RESPONSE_LIMIT, TimeUnit.SECONDS);
      ActorRef handler = getActor();
      Future<Object> futureResponse = Patterns.ask(handler, transactionRequest, time);
      BankTransactionResponse packagedResponse =
          (BankTransactionResponse) Await.result(futureResponse, time.duration());
      actors.put(handler, ActorStatus.AVAILABLE);

      transactionResponse = packagedResponse.getTransactionResponse();
      validTokens.remove(transactionRequest.getValidationToken());
    }

    String path = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        + "/transaction/" + transactionResponse.getMessageId();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setLocation(new URI(path));

    return new ResponseEntity<>(transactionResponse, httpHeaders, HttpStatus.CREATED);
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

  public static void sendStatus() {
    try {
      final String bankId = UUID.randomUUID().toString();
      final String host = "localhost";
      final String sourceURL = "http://localhost:8085/";
      final long update_time = 2000;

      ConnectionFactory factory = new ActiveMQConnectionFactory(
          "failover://tcp://" + host + ":61616");
      Connection connection = factory.createConnection();
      connection.setClientID("BFE" + UUID.randomUUID().toString());

      Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

      Queue queue = session.createQueue("bankStatus");
      MessageProducer producer = session.createProducer(queue);

      connection.start();

      new Thread(() -> {
        try {
          do {
            TimeUnit.MILLISECONDS.sleep(update_time);
            BankInfo bankInfoObject = new BankInfo(bankId, sourceURL);
            Message bankInfoMessage = session.createObjectMessage(bankInfoObject);
            producer.send(bankInfoMessage);
            System.out.println("msg sent : " + new Timestamp(new Date().getTime()));
          } while (true);
        } catch (JMSException | InterruptedException e) {
          e.printStackTrace();
        }
      }).start();

    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
}
