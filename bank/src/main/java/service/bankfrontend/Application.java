package service.bankfrontend;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import service.bank.BankActor;
import service.core.ActorStatus;


@SpringBootApplication
public class Application {

  public static ActorSystem system;
  private static final int default_number = 3;

  public static void main(String[] args) {

    System.out.println(args.length);
    System.out.println(Arrays.toString(args));
    if (args.length == 0) {
      args = new String[]{"-c", Integer.toString(default_number)};
    }

    system = ActorSystem.create();
    int actor_count = default_number;

    for (int i = 0; i < args.length; i++) {
      System.out.println(args[i]);
      if ("-c".equals(args[i])) {
        actor_count = Integer.parseInt(args[++i]);
      } else {
        System.out.println("Unknown flag " + args[i] + "/n");
        System.out.println();
        System.out.println("Valid flags are:");
        System.out.println("\t-c <actor count>\tSpecify the number of actors created at launch");
        System.exit(0);
      }

      while (0 <= actor_count) {
        String uniqueID = UUID.randomUUID().toString();
        ActorRef newActor = system.actorOf(Props.create(BankActor.class), uniqueID);
        BankRestFront.actors.put(newActor, ActorStatus.AVAILABLE);
        actor_count--;
      }

    }

    final String bankId = UUID.randomUUID().toString();
    BankRestFront.createInitialAccounts(bankId);
    BankRestFront.sendStatus(bankId);
    SpringApplication.run(Application.class, args);
  }
}
