package com.formssi.middleware.pbft;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;  
  
 
public class Client {  
	private static Logger logger = LoggerFactory.getLogger(Client.class);
    /** 
     * 处理服务端发回的对象，可实现该接口。 
     */  
    public static interface ObjectAction{  
        void doAction(Object obj,Client client);  
    }  
    public static final class DefaultObjectAction implements ObjectAction{
    	//根据服务器的返回值做判断，进行后续操作
        public void doAction(Object obj,Client client) {  
//        	 System.out.println("client default do nothing");  
        }  
    }  
    public static void main(String[] args) throws UnknownHostException, IOException {  
        String serverIp = "127.0.0.1";  
        int port = 65431;  
        Client client = new Client(serverIp,port);  
        
        client.start();
        
    }  
      
    private String serverIp;  
    private int port;  
    private Socket socket;  
    private boolean running=false;  
    private long lastSendTime;  
    private ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<Class,ObjectAction>();  
    ObjectOutputStream oos;
    public Client(String serverIp, int port) {  
        this.serverIp=serverIp;
        this.port=port;  
    }  
      
    public void start() throws UnknownHostException, IOException {  
        if(running)return;  
        socket = new Socket(serverIp,port);  
        oos = new ObjectOutputStream(socket.getOutputStream());
        logger.debug("本地端口："+socket.getLocalPort());  
        lastSendTime=System.currentTimeMillis();  
        running=true;  
       // new Thread(new KeepAliveWatchDog()).start();  
        new Thread(new ReceiveWatchDog()).start();  
    }  
      
    public void stop(){  
    	System.out.println("stop");
        if(running)running=false;  
    }  
      
    /** 
     * 添加接收对象的处理对象。 
     * @param cls 待处理的对象，其所属的类。 
     * @param action 处理过程对象。 
     */  
    public void addActionMap(Class<?> cls,ObjectAction action){  
        actionMapping.put(cls, action);  
    }  
  
    public void sendObject(Object obj)  {
        
		try {
			oos.writeObject(obj);  
			logger.debug("发送：\t"+obj);  
			oos.flush();  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }  
      
  /*  class KeepAliveWatchDog implements Runnable{  
        long checkDelay = 10;  
        long keepAliveDelay = 2000;  
        public void run() {  
            while(running){  
                if(System.currentTimeMillis()-lastSendTime>keepAliveDelay){  
                    try {  
                        Client.this.sendObject(new KeepAlive());  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                        Client.this.stop();  
                    }  
                    lastSendTime = System.currentTimeMillis();  
                }else{  
                    try {  
                        Thread.sleep(checkDelay);  
                    } catch (InterruptedException e) {  
                        e.printStackTrace();  
                        Client.this.stop();  
                    }  
                }  
            }  
        }  
    }  */
      
    class ReceiveWatchDog implements Runnable{  
        public void run() {  
        	try {
				InputStream in = socket.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(in);
				while (running) {
					if (in.available() > 0) {
						Object obj = ois.readObject();
						ObjectAction oa = actionMapping.get(obj.getClass());
						oa = oa == null ? new DefaultObjectAction() : oa;
						oa.doAction(obj, Client.this);
					} else {
						Thread.sleep(10);
					}
				} 
			} catch (Exception e) {
				// TODO: handle exception
			}  
            }  
    }
}  
