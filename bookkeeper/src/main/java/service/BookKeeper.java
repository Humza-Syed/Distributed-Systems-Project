package service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import java.util.HashMap;
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
import service.message.AddressBookRequest;
import service.message.AddressBookResponse;
import service.message.BankInfo;
import service.message.BookChange;

/**
 * This class keeps a record of all the active banks, and send updates of the bank list to the
 * subscribed atms via JMS queues and topics.
 */
public class BookKeeper {

  Cache<String, String> bankStatusCache = CacheBuilder.newBuilder()
      .expireAfterAccess(10, TimeUnit.SECONDS)
      .removalListener((RemovalListener<String, String>) removalNotification -> sendUpdateMessage(
          new BookChange(AddressBookChange.DROP,
              new BankInfo(removalNotification.getKey(), removalNotification.getValue()))))
      .build();

  private final static String BANK_STATUS_QUEUE = "bankStatus";
  private final static String ADDRESS_BOOK_UPDATE_QUEUE = "addressBookUpdate";
  private final static String BANK_AB_QUEUE = "addressBookRequest";
  private final static String BANK_AB_TOPIC = "fullAddressBook";
  private final static int queueTimeThreshold = 2500;

  private Connection bankStatusConnection;
  private Session bankStatusSession;
  private MessageConsumer bankStatusConsumer;
  private MessageProducer updateBookProducer;
  private MessageConsumer bankAddressBookConsumer;
  private MessageProducer bankAddressBookProducer;

  public BookKeeper() {
  }

  /**
   * This method runs the BookKeeper class.
   *
   * @param bookKeeperHost The host for the ActiveMQ connection.
   * @throws JMSException Thrown when an issue occurs within JMS.
   */
  public void start(String bookKeeperHost) throws JMSException {
    initialise(bookKeeperHost);

    try {
      bankStatusConnection.start();

      // Thread for receiving BankStatusMessages
      new Thread(this::bankInfoMessageHandler).start();

      // Thread for processing addressBookMessages
      new Thread(this::fullAddressBookMessageHandler).start();

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

  public void publishAddressBook() {
    try {
      HashMap<String, String> addressBook = new HashMap<>(bankStatusCache.asMap());

      AddressBookResponse newABResponse = new AddressBookResponse(addressBook);
      Message currentAddressBook = bankStatusSession.createObjectMessage(newABResponse);

      bankAddressBookProducer.send(currentAddressBook);
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }


  /**
   * This method initialises all the JMS connections: connections, sessions, etc.
   *
   * @param bookKeeperHost The host for the connection used by the bookKeeper.
   * @throws JMSException Throws exception when an issue occurs within JMS.
   */
  //TODO Move the connections to different connections.
  private void initialise(String bookKeeperHost) throws JMSException {
    // Initialising the bankStatus connection
    ConnectionFactory bankStatusConnectionFactory = new ActiveMQConnectionFactory(
        "failover://tcp://" + bookKeeperHost + ":61616");
    bankStatusConnection = bankStatusConnectionFactory.createConnection();
    bankStatusConnection.setClientID("BookKeeper");
    bankStatusSession = bankStatusConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

    // Queue for receiving bankStatusMessages from banks.
    Queue bankStatusQueue = bankStatusSession.createQueue(BANK_STATUS_QUEUE);
    bankStatusConsumer = bankStatusSession.createConsumer(bankStatusQueue);

    //Topic for sending incremental updates to the address book when updates occur.
    Topic updateBookTopic = bankStatusSession.createTopic(ADDRESS_BOOK_UPDATE_QUEUE);
    updateBookProducer = bankStatusSession.createProducer(updateBookTopic);

    Queue bankAddressBookQueue = bankStatusSession.createQueue(BANK_AB_QUEUE);
    bankAddressBookConsumer = bankStatusSession.createConsumer(bankAddressBookQueue);

    Topic bankAddressBookTopic = bankStatusSession.createTopic(BANK_AB_TOPIC);
    bankAddressBookProducer = bankStatusSession.createProducer(bankAddressBookTopic);
  }

  private void sendUpdateMessage(BookChange bookChange) {
    /* Added exception handling here due to cache builder needing exception handling within this method.
    Ideally would have better exception handling then just printing the stack trace.
     */
    try {
      updateBookProducer.send(bankStatusSession.createObjectMessage(bookChange));
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

  private void fullAddressBookMessageHandler() {
    try {
      do {
        Message message = bankAddressBookConsumer.receive();
        if (message instanceof ObjectMessage) {
          Object content = ((ObjectMessage) message).getObject();
          if (content instanceof AddressBookRequest) {
            message.acknowledge();
            long threshold = System.currentTimeMillis() + queueTimeThreshold;

            while (System.currentTimeMillis() < threshold) {
              message = bankAddressBookConsumer.receiveNoWait();
              if (message == null) {
                break;
              }
              message.acknowledge();
            }
            publishAddressBook();
          }
        }
      } while (true);
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

  private void bankInfoMessageHandler() {
    try {
      do {
        Message message = bankStatusConsumer.receive();
        if (message instanceof ObjectMessage) {
          Object content = ((ObjectMessage) message).getObject();
          if (content instanceof BankInfo) {
            BankInfo bankInfoMessage = (BankInfo) content;

            String t = bankStatusCache.getIfPresent(bankInfoMessage.getBankId());
            if (t == null) {
              bankStatusCache.put(bankInfoMessage.getBankId(), bankInfoMessage.getUrl());
              sendUpdateMessage(new BookChange(AddressBookChange.INSERT,
                  new BankInfo(bankInfoMessage.getBankId(), bankInfoMessage.getUrl())));
            }
            message.acknowledge();
          }
        }
      } while (true);
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws JMSException {
    String host = "localhost";
    if (args.length > 0) {
      host = args[0];
    }
    BookKeeper bookKeeper = new BookKeeper();
    bookKeeper.start(host);
  }
}
