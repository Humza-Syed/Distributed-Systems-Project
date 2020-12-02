package service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import java.util.concurrent.TimeUnit;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;
import service.message.AddressBookChange;
import service.message.BankIndexElement;
import service.message.BankStatusMessage;
import service.message.BookChange;

public class BookKeeper {

  Cache<String, String> bankStatusCache = CacheBuilder.newBuilder()
      .maximumSize(5)
      .expireAfterAccess(10, TimeUnit.SECONDS)
      .removalListener((RemovalListener<String, String>) removalNotification -> sendUpdateMessage(
          removalNotification.getKey(), removalNotification.getValue()))
      .build();

  private final static String BANK_STATUS_QUEUE = "bankStatus";

  private Connection bankStatusConnection;
  private MessageConsumer bankStatusConsumer;

  public BookKeeper() {
  }

  public void start(String bookKeeperHost) throws JMSException {
    initialise(bookKeeperHost);

    try {
      bankStatusConnection.start();
      // Thread for receiving BankStatusMessage
      new Thread(() -> {
        try {
          do {
            Message message = bankStatusConsumer.receive();
            if (message instanceof ObjectMessage) {
              Object content = ((ObjectMessage) message).getObject();
              if (content instanceof BankStatusMessage) {
                BankStatusMessage inputMessage = (BankStatusMessage) content;

                String t = bankStatusCache.getIfPresent(inputMessage.getMessageId());
                if (t == null) {
                  bankStatusCache.put(inputMessage.getMessageId(), inputMessage.getUrl());
                }

                message.acknowledge();
              }
            }
          } while (true);
        } catch (JMSException e) {
          e.printStackTrace();
        }
      }).start();
    } catch (JMSException e) {
      e.printStackTrace();
    }

    // This thread will cleanup all expired entries.
    while (true) {
      bankStatusCache.cleanUp();
      try {
        Thread.sleep(2500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void initialise(String bookKeeperHost) throws JMSException {
    // Initialising the bankStatus connection
    ConnectionFactory bankStatusConnectionFactory = new ActiveMQConnectionFactory(
        "failover://tcp://" + bookKeeperHost + ":61616");
    bankStatusConnection = bankStatusConnectionFactory.createConnection();
    bankStatusConnection.setClientID("BookKeeper");
    Session bankStatusSession = bankStatusConnection
        .createSession(false, Session.CLIENT_ACKNOWLEDGE);
    Queue bankStatusQueue = bankStatusSession.createQueue(BANK_STATUS_QUEUE);
    bankStatusConsumer = bankStatusSession.createConsumer(bankStatusQueue);
  }

  private void sendUpdateMessage(String key, String value) {
    //TODO Send update message via the topic
    System.out.println("Bank was removed from cache: " + key + " value: " + value);
  }

  public static void main(String[] args) throws JMSException {
    String host = "localhost";
    if (args.length > 0) {
      host = args[0];
    }

    BookKeeper bookKeeper = new BookKeeper();
    bookKeeper.start(host);
  }

  public static Boolean bankIndexExistenceCheck(BankIndexElement element) {
    //TODO check element existence in local storage + reset timetodie

    return true;
  }
}
