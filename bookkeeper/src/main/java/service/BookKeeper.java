package service;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import service.message.BankStatusMessage;

public class BookKeeper {

  public static void main(String[] args) {
    String host = "localhost";
    if (args.length > 0) {
      host = args[0];
    }
    try {
      ConnectionFactory factory = new ActiveMQConnectionFactory(
          "failover://tcp://" + host + ":61616");
      Connection connection = factory.createConnection();
      connection.setClientID("BookKeeper");
      Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

      Queue queue = session.createQueue("bankStatus");
      MessageConsumer consumer = session.createConsumer(queue);

      connection.start();

      new Thread(() -> {
        try {
          do {
            Message message = consumer.receive();
            System.out.println("Msg received");
            if (message instanceof ObjectMessage) {
              Object content = ((ObjectMessage) message).getObject();
              if (content instanceof BankStatusMessage) {
                BankStatusMessage inputMessage = (BankStatusMessage) content;
                System.out.println(
                    "MSG_ID: " + inputMessage.getMessageId() + " URL: " + inputMessage.getUrl()
                        + "\n");
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
  }
}
