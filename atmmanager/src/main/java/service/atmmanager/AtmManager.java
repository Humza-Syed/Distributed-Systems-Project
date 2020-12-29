package service.atmmanager;

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
import service.message.HeartbeatRequest;
import service.message.HeartbeatResponse;
import service.message.LowAtmBalanceRequest;
import service.message.LowAtmBalanceResponse;

public class AtmManager {

  public static final String DEFAULT_HOST_NAME = "localhost";
  public static final double DEFAULT_ATM_BALANCE = 10000;
  public static final int HEARTBEAT_PAUSE_TIME = 1000;
  public static final String HEARTBEAT_REQUEST_TOPIC = "atmToAtmManagerMessageTopic";
  public static final String HEARTBEAT_RESPONSE_QUEUE = "atmToAtmManagerMessageQueue";
  private static long healthCheckCount = 0;

  private Connection connection;
  private MessageConsumer atmManagerMessageConsumer;
  private MessageProducer atmManagerMessageProducer;
  private Session session;

  Cache<String, String> activeAtmCache = CacheBuilder.newBuilder()
      .expireAfterAccess(5, TimeUnit.SECONDS)
      .removalListener((RemovalListener<String, String>) potentialRemoval -> fixAtm(
          potentialRemoval.getKey(), potentialRemoval.getValue())).build();

  public static void main(String[] args) {
    String hostName = (args.length == 0) ? DEFAULT_HOST_NAME : args[0];
    AtmManager manager = new AtmManager();
    manager.start(hostName);
  }

  public AtmManager() {}

  private void start(String hostName) {
    try {
      initialize(hostName);
      connection.start();

      // starting thread for performing the check up
      new Thread(() -> {
        try {
          while (true) {
            HeartbeatRequest heartbeatRequest = new HeartbeatRequest(
                Integer.toString((int) healthCheckCount++));
            Message packagedHeartbeatRequest = session.createObjectMessage(heartbeatRequest);
            atmManagerMessageProducer.send(packagedHeartbeatRequest);
            TimeUnit.MILLISECONDS.sleep(HEARTBEAT_PAUSE_TIME);
            activeAtmCache.cleanUp();
          }
        } catch (JMSException | InterruptedException e) {
          e.printStackTrace();
        }
      }).start();

      // thread which receives messages from the atms
      while (true) {
        Message message = atmManagerMessageConsumer.receive();
        if (message instanceof ObjectMessage) {
          Object content = ((ObjectMessage) message).getObject();

          if (content instanceof HeartbeatResponse) {
            HeartbeatResponse heartbeatResponse = (HeartbeatResponse) content;
            String currentResponsesUrl = activeAtmCache.getIfPresent(heartbeatResponse.getAtmId());
            if (currentResponsesUrl == null) {
              activeAtmCache.put(heartbeatResponse.getAtmId(), heartbeatResponse.getAtmUrl());
              System.out.println("atmmanager: meeting a new atm, registering it");
            }

          } else if (content instanceof LowAtmBalanceRequest) {
            LowAtmBalanceRequest lowAtmBalanceRequest = (LowAtmBalanceRequest) content;
            String currentResponsesUrl = activeAtmCache
                .getIfPresent(lowAtmBalanceRequest.getAtmId());

            // the atm manager will only reply to a LowBalanceRequest if it is from an atm which it manages
            if (currentResponsesUrl != null) {
              atmManagerMessageProducer.send(session
                  .createObjectMessage(new LowAtmBalanceResponse(DEFAULT_ATM_BALANCE,
                      lowAtmBalanceRequest.getAtmId())));
              System.out.println("atmmanager: low balance request received");
            }
          }
        }
        message.acknowledge();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initialize(String hostName) throws JMSException {
    // setting up the jms pathways for the atm health check up/heartbeats
    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
        "failover://tcp://" + hostName + ":61616");
    connection = connectionFactory.createConnection();
    connection.setClientID("AtmManager");
    session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

    Queue heartbeatResponseQueue = session.createQueue(HEARTBEAT_RESPONSE_QUEUE);
    atmManagerMessageConsumer = session.createConsumer(heartbeatResponseQueue);

    Topic heartbeatRequestTopic = session.createTopic(HEARTBEAT_REQUEST_TOPIC);
    atmManagerMessageProducer = session.createProducer(heartbeatRequestTopic);
  }


  private void fixAtm(String atmId, String atmUrl) {
    System.out.println("atmmanager: atm[" + atmId + "] gone offline, sending repair team.");
  }
}