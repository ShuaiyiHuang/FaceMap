package Server;


import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

//import Configuration.Configuration;
import reuse.cm.ReadJson;
import reuse.pm.PMManager;
//import com.HaroldLIU.LicenseManager;
import reuse.license.MaxNumOfMessage;

/**
 * Created by Harold_LIU on 3/20/16.
 */
public class Server {
	//��Ҫ���ñ���
	static public String port;
	static public int timeGap;
	static public int maxRequestTimes;

    static private List<User> users = new ArrayList<User>();
    static private Server server;
    String userName;
    String passWord;
    boolean state=false;

    //PerformanceManager performanceManager = new PerformanceManager("testLog.txt",60*1000);
    PMManager pmManager=new PMManager("/Users/nyt/Desktop/",1);
   // LicenseManager licenseManager = new LicenseManager();



    private void userInit()
    {
        users.add(new User("liu","123",false,0,0,0));
        users.add(new User("zhao","123",false,0,0,0));
        users.add(new User("qian","123",false,0,0,0));
        users.add(new User("sun","123",false,0,0,0));
        users.add(new User("li", "123", false, 0, 0, 0));
    }

    private   Server ()
    {
        userInit();
        //performanceManager.start();
        pmManager.startRecord();
        //licenseManager.ThroughputInit(timeGap,maxRequestTimes,0);
       // licenseManager.CapacityInit(100,0);
      //MaxNumOfMessage maxNumOfMessage = new MaxNumOfMessage(100);
    }

    private User findUser(String userName)
    {
        for (int i = 0; i<users.size();i++) {
            if (userName.equals(users.get(i).UserName)) {
                return users.get(i);
            }
        }
        return null;
    }

    public static Server sharedServer()
    {
        if (server == null)
        {
            server = new Server();
        }
        return server;
    }

    public int login (String userName, String password)
    {
    	MaxNumOfMessage maxNumOfMessage = new MaxNumOfMessage(100);
        User theUser = findUser(userName);
        if(!theUser.equals(null))
        {
            //if (licenseManager.ThroughputCheck())
        	if (maxNumOfMessage.Check())
            {
                if (password.equals(theUser.Password)) {
                    //performanceManager.successTime++;
                	pmManager.LogSuccess();
                    theUser.isLogin = true;

                    return 200;
                }
                else
                {
                    //performanceManager.failTime++;
                	pmManager.LogFail();
                    return 201;
                }
            }
            else
            {
                //performanceManager.failTime++;
            	pmManager.LogFail();
                return 203;
            }
        }
        else
        {
            //performanceManager.failTime ++;
        	pmManager.LogFail();
            return 202;
        }
    }

    public boolean checkConnection(String userName)
    {
        User theUser = findUser(userName);

        if(!theUser.equals(null))
        {
            if (theUser.isLogin)
                return  true;
            else
                return false;
        }
        else
            return false;

    }

    class Listen extends Thread{
    	String topicName;
    public Listen(String _topicName){
    	topicName=_topicName;
    }
    public void ListenMsg(){

    	ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(port);
        Connection connection;
		try {
			connection = factory.createConnection();

		    connection.start();

		    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		    Destination topic = session.createTopic(topicName);
		    MessageConsumer consumer = session.createConsumer(topic);

		    consumer.setMessageListener(new MessageListener() {
			public void onMessage(Message msg) {

				TextMessage txtMsg = (TextMessage) msg;

				try {

					if(topicName.equals("userName")){
						userName=txtMsg.getText();
					}else{
						passWord=txtMsg.getText();
						if(!state) state=true;
					}
						if(state){
							int pos=userName.indexOf(':');
							String name=userName.substring(0, pos);
							String username=userName.substring(pos+1,userName.length());
							String password=passWord.substring(pos+1,passWord.length());
							int back=login(username,password);
							sendMsg(String.valueOf(back),name);
							state=false;
						}
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		    });
			} catch (JMSException e1) {
				e1.printStackTrace();
			}

    }
    public void run(){ListenMsg();}

    }

    public  void sendMsg(String msgText,String toipcName){
        try {
    	ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(port);

		Connection connection = factory.createConnection();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Destination dest = session.createTopic(toipcName);
		MessageProducer producer = session.createProducer(dest);


		TextMessage msg = session.createTextMessage();
		msg.setText(msgText);
		producer.send(msg);
        System.out.println(msg.getText());

			} catch (JMSException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
    }
    public static void main(String[] args) throws Exception {
    	//port = "tcp://localhost:61616"; //+ Configuration.getPort();
    	//timeGap = 1000;// Integer.parseInt(1000);
    	//maxRequestTimes = 5;// Integer.parseInt(Configuration.getMaxRequestTimes());

    	port = "tcp://localhost:" + ReadJson.GetConfig("port", "D:\\Mars_Workspace\\Ericsson\\sets.txt");
    	timeGap = Integer.parseInt(ReadJson.GetConfig("timeGap", "D:\\Mars_Workspace\\Ericsson\\sets.txt"));
    	maxRequestTimes = Integer.parseInt(ReadJson.GetConfig("maxRequestTimes", "D:\\Mars_Workspace\\Ericsson\\sets.txt"));
    	
    	Server server=Server.sharedServer();
    	Listen userName=server.new Listen("userName");
    	userName.start();
    	Listen password=server.new Listen("passWord");
    	password.start();
    	System.out.println("--------Server Start------");

    }

}
