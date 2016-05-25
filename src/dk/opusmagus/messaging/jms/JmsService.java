package dk.opusmagus.messaging.jms;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.xml.ws.ServiceMode;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

@Service("jmsService")
public class JmsService {
	
	//@Resource(lookup = "jms/ConnectionFactory")
	@Resource
	private ConnectionFactory connectionFactory;
	
	//@Resource(lookup = "jms/Queue")
	private Queue queue;

	//@Resource(lookup = "jms/Topic")
	private Topic topic;
	
	public Connection getConnection() { 
		try {
			Connection conn = connectionFactory.createConnection();
			return conn;
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
