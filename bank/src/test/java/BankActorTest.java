import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import bankmessage.BankTransactionRequest;
import bankmessage.BankTransactionResponse;
import org.junit.Before;
import org.junit.Test;
import service.bank.BankActor;
import service.message.TransactionRequest;
import service.message.TransactionType;
import service.message.ValidationRequest;
import service.message.ValidationResponse;

/**
 * These tests are basic tests to ensure that the actor can receive these message types and respond
 * to them.
 */
public class BankActorTest {

  private ActorSystem testActorSystem;
  private ActorRef testBankActorRef;
  private TestKit testKit;

  @Before
  public void setUp() {
    testActorSystem = ActorSystem.create("TestSystem");
    testBankActorRef = testActorSystem.actorOf(Props.create(BankActor.class), "test");
    testKit = new TestKit(testActorSystem);
  }

  @Test
  public void CreateReceive_ValidationRequest_ValidationResponse() {
    testBankActorRef.tell(new ValidationRequest(1, 1000), testKit.getRef());
    testKit.awaitCond(testKit::msgAvailable);
    testKit.expectMsgClass(ValidationResponse.class);
  }

  @Test
  public void CreateReceive_BankTransactionRequest_BankTransactionResponse() {
    testBankActorRef.tell(new TransactionRequest(1, 1, TransactionType.DEPOSIT, 100, "token"),
        testKit.getRef());
    testKit.awaitCond(testKit::msgAvailable);
    testKit.expectMsgClass(BankTransactionResponse.class);
  }
}
