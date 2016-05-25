package dk.opusmagus.messaging.jms;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.ws.ServiceMode;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.integration.transports.netty.NettyConnectorFactory;
import org.hornetq.integration.transports.netty.TransportConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class JmsTutorialContext {
	
	private Session session = null;
	private Connection connection = null;
	private Dispatcher dispatcher = null;	
	
	public JmsTutorialContext() throws JMSException {		       		       
	       
	       Map connectionParams = new HashMap();
	       connectionParams.put(TransportConstants.PORT_PROP_NAME, 5445);
	       connectionParams.put(TransportConstants.HOST_PROP_NAME, "nb-chzu-0265.int.audatex.com");
	       //connectionParams.put(TransportConstants.DEFAULT_HOST, "nb-chzu-0265.int.audatex.com");
	       
	       TransportConfiguration transportConfiguration = new TransportConfiguration(
	    		   NettyConnectorFactory.class.getName(),
                   connectionParams);
	       
	       ConnectionFactory cf = HornetQJMSClient.createConnectionFactory(transportConfiguration);
	       
	       
	       connection = cf.createConnection();
	       
	       session = connection.createSession(true, Session.SESSION_TRANSACTED);
	       connection.start();
	}

	public void putMessage(String messageContent) {			
		
		   try
		   {
			   System.out.println("putMessage() called...");
			   //StandaloneHornetQServer.current().run();
		       // Step 1. Directly instantiate the JMS Queue object.

			   String sourceQueueName = "sgi-data";		   
		       Queue sourceQ = HornetQJMSClient.createQueue(sourceQueueName);
		       MessageProducer producer = session.createProducer(sourceQ);		       
		       TextMessage message = session.createTextMessage(messageContent);		       
		       producer.send(message);	
		       session.commit();
		       
		       //consumeMessages();
		       
		       //String qName = queue.getQueueName();
		       
		       // Step 2. Instantiate the TransportConfiguration object which contains the knowledge of what transport to use,
		       // The server port etc.

		       /*Map connectionParams = new HashMap();
		       connectionParams.put(PORT_PROP_NAME, 5445);

		       TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(),
		                                                                                  connectionParams);

		       // Step 3 Directly instantiate the JMS ConnectionFactory object using that TransportConfiguration
		       ConnectionFactory cf = HornetQJMSClient.createConnectionFactory(transportConfiguration);

		       // Step 4.Create a JMS Connection
		       connection = cf.createConnection();

		       // Step 5. Create a JMS Session
		       Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		       // Step 6. Create a JMS Message Producer
		       MessageProducer producer = session.createProducer(queue);

		       // Step 7. Create a Text Message
		       TextMessage message = session.createTextMessage("This is a text message");

		       System.out.println("Sent message: " + message.getText());

		       // Step 8. Send the Message
		       producer.send(message);

		       // Step 9. Create a JMS Message Consumer
		       MessageConsumer messageConsumer = session.createConsumer(queue);

		       // Step 10. Start the Connection
		       connection.start();

		       // Step 11. Receive the message
		       TextMessage messageReceived = (TextMessage)messageConsumer.receive(5000);*/

		       //System.out.println("Received message: " + messageReceived.getText());
		       		       
		   }
		   catch(Exception e)
		   {
			   e.printStackTrace();
		   }
		   finally
		   {
			   if(dispatcher != null)
				   dispatcher.stop();
			   
		      if (connection != null)
		      {
		    	  try {
					connection.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
		      }
		   }
	}

	private void consumeMessages() throws JMSException {		
	   String consumerQueueName1 = "cq1";
	   String consumerQueueName2 = "cq2";
	   String consumerQueueName3 = "cq3";
		   
       Queue consumerQ1 = HornetQJMSClient.createQueue(consumerQueueName1);
       Queue consumerQ2 = HornetQJMSClient.createQueue(consumerQueueName2);
       Queue consumerQ3 = HornetQJMSClient.createQueue(consumerQueueName3);
       
		//MessageConsumer sourceConsumer = session.createConsumer(sourceQ);
       MessageConsumer messageConsumer1 = session.createConsumer(consumerQ1);
       MessageConsumer messageConsumer2 = session.createConsumer(consumerQ2);
       MessageConsumer messageConsumer3 = session.createConsumer(consumerQ3);
       
       
       String[] consumerQueues = new String[]{"consumer1", "consumer2", "consumer3"};
       //dispatcher = new Dispatcher(cf, sourceQueueName, consumerQueues);
       //dispatcher.start();	      		       		       
       
       TextMessage messageReceived;
       //TextMessage messageReceived = (TextMessage)sourceConsumer.receive(5000);
       //System.out.println("Source Consumer received message: " + messageReceived.getText());
       
       messageReceived = (TextMessage)messageConsumer1.receive(5000);
       System.out.println("Consumer1 received message: " + messageReceived.getText());
       
       messageReceived = (TextMessage)messageConsumer2.receive(5000);
       System.out.println("Consumer2 received message: " + messageReceived.getText());
       
       messageReceived = (TextMessage)messageConsumer3.receive(5000);
       System.out.println("Consumer3 received message: " + messageReceived.getText());
	}
	
	public static void main(String[] args) {
		ApplicationContext context;
		try {
			//ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
			//context = new ClassPathXmlApplicationContext("context.xml");
			//JmsService service = (JmsService)context.getBean("jmsService");

			//Connection conn = service.getConnection();
			//Connection conn = connectionFactory.createConnection();
			JmsTutorialContext myContext = new JmsTutorialContext();
			myContext.putMessage("{ msg: Hello Robbert again!}");
			//myContext.consumeMessages();
			
			//StandaloneHornetQServer.current().requestShutdown();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			//if(context != null)
				//context.
		}
	}
}
