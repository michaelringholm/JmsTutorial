package dk.opusmagus.messaging.jms;
 
import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
 
public class StandaloneHornetQServer implements Runnable
{
	private static StandaloneHornetQServer current = null;
	private boolean shutdownRequested = false;
	private HornetQServer server;
	private boolean running = false;
	
	private StandaloneHornetQServer()
	{
	}
	
	public static synchronized StandaloneHornetQServer current() {
		if(current == null)
			current = new StandaloneHornetQServer();
		
		return current;
	}
	
	public synchronized void requestShutdown() {
		shutdownRequested = true;
		System.out.println("HornetQ shutdown requested!");
	}
	
	private void start() throws Exception {

            //Load the file configuration first of all
            FileConfiguration configuration = new FileConfiguration();
            configuration.setConfigurationUrl("hornetq-configuration.xml");
            configuration.start();
 
            //Create a new instance of hornetq server
            server = HornetQServers.newHornetQServer(configuration);
 
            //Wrap inside a JMS server
            JMSServerManager jmsServerManager = new JMSServerManagerImpl(server, "hornetq-jms.xml");
 
            // if you want to use JNDI, simple inject a context here or don't
            // call this method and make sure the JNDI parameters are set.
            jmsServerManager.setContext(null);
 
            //Start the server
            jmsServerManager.start();
 
            System.out.println("HornetQ server started successfully!");
	}
	
	private void stop() throws Exception {
		server.stop();		
		System.out.println("HornetQ server stopped!");
	}
	
    public static void main(String[] args) throws Exception {
    	StandaloneHornetQServer.current().run();
    }

	@Override
	public void run() {
		if(!StandaloneHornetQServer.current().running)
		{
	    	try
	        {
	    		StandaloneHornetQServer.current().start();
	    		StandaloneHornetQServer.current().running = true;
	        	while(!StandaloneHornetQServer.current().shutdownRequested)
	        	{
	        		Thread.sleep(1000);
	        	}        	
	        }
	        catch (Throwable e)
	        {
	            System.out.println("Well, you seems to doing something wrong. Please check if config files are in your classes folder.");
	            e.printStackTrace();
	        }
	    	finally {
	    		try {
					StandaloneHornetQServer.current().stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
		}
	}
}