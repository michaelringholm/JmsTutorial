package dk.opusmagus.messaging.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.integration.transports.netty.NettyConnectorFactory;
import org.hornetq.integration.transports.netty.TransportConstants;

public class Dispatcher {

	private static long QUEUE_WAIT_TIME = 5000;
	private boolean mStop = false;
	private ConnectionFactory mFactory;
	private String mSourceQueueName;
	private String[] mConsumerQueueNames;

	public static void main(String[] args)
	{
		System.out.println("Starting Jms dispatcher...");
	       Map connectionParams = new HashMap();
	       connectionParams.put(TransportConstants.PORT_PROP_NAME, 5445);
	       //connectionParams.put(TransportConstants.HOST_PROP_NAME, "nb-chzu-0265.int.audatex.com");
	       
	       TransportConfiguration transportConfiguration = new TransportConfiguration(
	    		   NettyConnectorFactory.class.getName(),
                connectionParams);
	       
	       ConnectionFactory cf = HornetQJMSClient.createConnectionFactory(transportConfiguration);
	       
	       String sourceQueueName = "sgi-data";
	       String[] consumerQueues = new String[]{"cq1", "cq2", "cq3"};
	       Dispatcher disp = new Dispatcher(cf, sourceQueueName, consumerQueues);
	       disp.run();
	       System.out.println("Jms dispatcher stopped!");
	}
	/**
	 * Create a dispatcher
	 * 
	 * @param factory
	 *            The QueueConnectionFactory in which new connections, session,
	 *            and consumers will be created. This is needed to ensure the
	 *            connection is associated with the correct thread.
	 * @param source
	 * 
	 * @param consumerQueues
	 */
	public Dispatcher(ConnectionFactory factory, String sourceQueue,
			String[] consumerQueues) {

		mFactory = factory;
		mSourceQueueName = sourceQueue;
		mConsumerQueueNames = consumerQueues;
	}
	
	public void dispatch(Session session, Queue sourceQ, MessageConsumer consumer) throws Exception {
		MessageProducer producer = session.createProducer(null);

		Queue[] destinationQueues = new Queue[mConsumerQueueNames.length];
		for (int index = 0; index < mConsumerQueueNames.length; ++index) {
			destinationQueues[index] = session.createQueue(mConsumerQueueNames[index]);
		}
		
		Message m = consumer.receive(QUEUE_WAIT_TIME);

		for (Queue q : destinationQueues) {
			producer.send(q, m);
		}
	}

	public void start() {
		Thread thread = new Thread(new Runnable() {

			public void run() {
				Dispatcher.this.run();
			}
		});
		thread.setName("Queue Dispatcher");
		thread.start();
	}

	public void stop() {
		mStop = true;
	}

	private void run() {

		Connection connection = null;
		MessageProducer producer = null;
		MessageConsumer consumer = null;
		Session session = null;

		try {

			Map connectionParams = new HashMap();
			connectionParams.put(TransportConstants.PORT_PROP_NAME, 5445);

			TransportConfiguration transportConfiguration = new TransportConfiguration(
					NettyConnectorFactory.class.getName(), connectionParams);

			ConnectionFactory cf = HornetQJMSClient.createConnectionFactory(transportConfiguration);
			connection = cf.createConnection();
			session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
			
			Queue sourceQueue = session.createQueue(mSourceQueueName);
			consumer = session.createConsumer(sourceQueue);

			// Create a null producer allowing us to send messages
			// to any queue.
			producer = session.createProducer(null);

			// Create the destination queues based on the consumer names we
			// were given.
			Queue[] destinationQueues = new Queue[mConsumerQueueNames.length];
			for (int index = 0; index < mConsumerQueueNames.length; ++index) {
				destinationQueues[index] = session.createQueue(mConsumerQueueNames[index]);
			}

			connection.start();

			while (!mStop) {

				// Only wait QUEUE_WAIT_TIME in order to give
				// the dispatcher a chance to see if it should
				// quit
				Message m = consumer.receive(QUEUE_WAIT_TIME);
				if (m == null) {
					System.out.println("The dispatcher found no message on the queue :-(");
					continue;
				}
				System.out.println("The dispatcher found a message on the queue :-)");

				// Take the message we received and put
				// it in each of the consumers destination
				// queues for them to process
				for (Queue q : destinationQueues) {
					producer.send(q, m);
					System.out.println("The dispatcher is dispatching to [" + q.getQueueName() + "]");
				}
				//session.commit();
			}

		} catch (JMSException ex) {
			ex.printStackTrace();
		} finally {
			if (producer != null) {
				try {
					producer.close();
				} catch (JMSException ex) {
				}
			}
			if (consumer != null) {
				try {
					consumer.close();
				} catch (JMSException ex) {
				}
			}
			if (session != null) {
				try {
					session.close();
				} catch (JMSException ex) {
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException ex) {
				}
			}
		}
	}
}