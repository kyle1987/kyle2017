package com.formssi.middleware.pbft;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formssi.middleware.encrypt.RSACoder;
import com.formssi.middleware.utils.PropertiesUtil;  
  
/** 
 * C/S架构的服务端对象。 
 * <p> 
 * 创建时间：2010-7-18 上午12:17:37 
 * @author HouLei 
 * @since 1.0 
 */  
public class Server {  
  
	
	private static Logger logger = LoggerFactory.getLogger(Server.class);
    /** 
     * 要处理客户端发来的对象，并返回一个对象，可实现该接口。 
     */  
    public interface ObjectAction{  
        Object doAction(Object rev);  
    }  
      
    public static final class DefaultObjectAction implements ObjectAction{  
        public Object doAction(Object rev) {  
        	logger.info("server default do nothing");  
            return rev;  
        }  
    }  
    
    private int port;  
    private String ip;
    private volatile boolean running=false;  
    private static ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<Class,ObjectAction>();  
    private Thread connWatchDog;  
    private static Map keyMap = null;
    
    private static Map<Integer,Client> nodeMap = new HashMap<>();
    
    private static Server server = null;
    private static Node self = null;
    
    public static void main(String[] args) {  
        int port = 65431;  
        server = new Server(port);
        try {
           String locations = PropertiesUtil.readValue("org.common.register.locations");
           keyMap = RSACoder.initKey();
           server.addActionMap(Message.class, new MessageAction());
           server.start(); 
           
           if(locations != null) {
        	   Node node =  new Node();
        	   node.setNodeNum(Integer.valueOf(locations.split(":")[0]));
        	   node.setIp(locations.split(":")[1]);
        	   node.setPort(Integer.valueOf(locations.split(":")[2]));
        	   addNode(node);
           }
 		   
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }  
    public Server(int port) {  
        this.port = port;  
    }  
  
	public void start(){  
        if(running)return;  
        running=true;  
        CountDownLatch latch = new CountDownLatch(1);
        connWatchDog = new Thread(new ConnWatchDog(latch));  
        connWatchDog.start(); 
        try {
        	latch.await();
        	self = new Node();
        	self.setIp(ip);
        	self.setPort(port);
			String nodeNumStr = PropertiesUtil.readValue("org.common.node.num") ;
			int nodeNum = (nodeNumStr == null )? 0: Integer.valueOf(nodeNumStr);
			self.setNodeNum(nodeNum);
		} catch (NumberFormatException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        allNodeList.add(self);
        checkTimeout();
        //test();
    }  
      
    /*public void test() {
    	  new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Message msg = new Message();
		    	msg.setMsgType("2");
		    	Transatcion t  =  new Transatcion();
		    	t.setFcn("query");
		    	t.setType("1");
		    	msg.setBody(t);
		    	try {
					client1.sendObject(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
    		  
    	  }).start();
    	
    }*/
    @SuppressWarnings("deprecation")  
    public void stop(){  
        if(running)running=false;  
        if(connWatchDog!=null)connWatchDog.stop();  
    }  
      
    public void addActionMap(Class<?> cls,ObjectAction action){  
        actionMapping.put(cls, action);  
    }  
      
    class ConnWatchDog implements Runnable{  
    	private CountDownLatch latch;
    	ConnWatchDog(CountDownLatch latch){
    		this.latch = latch;
    	}
        @SuppressWarnings("resource")
		public void run(){  
            try {  
                ServerSocket ss = new ServerSocket(port,5); 
                server.ip = ss.getInetAddress().getHostAddress().equals("0.0.0.0")  ? "localhost":ss.getInetAddress().getHostAddress();
                latch.countDown();
                while(running){  
                    Socket s = ss.accept();  
                    new Thread(new SocketAction(s)).start();  
                }  
            } catch (IOException e) {  
                e.printStackTrace();  
                Server.this.stop();  
            }  
              
        }  
    }  
      
    class SocketAction implements Runnable{  
        Socket s;  
        boolean run=true;  
        long lastReceiveTime = System.currentTimeMillis();  
        public SocketAction(Socket s) {  
            this.s = s;  
        }  
        public void run() {  
        	try {  
        	 InputStream in = s.getInputStream();  
        	 ObjectInputStream ois = new ObjectInputStream(in); 
        	 ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());  
            while(running && run){  
//                if(System.currentTimeMillis()-lastReceiveTime>receiveTimeDelay){  
//                    overThis();  
//                }else{  
                    	//System.out.println("host address:" + s.getInetAddress().getHostAddress());
                       
                        if(in.available()>0){  
                            
                            Object obj = ois.readObject();  
                            lastReceiveTime = System.currentTimeMillis();  
                            logger.debug("server接收：\t"+obj);  
                            ObjectAction oa = actionMapping.get(obj.getClass());  
                            oa = oa==null?new DefaultObjectAction():oa;  
                            Object out = oa.doAction(obj);  
                            if(out!=null){  
                            	
                                oos.writeObject(out);  
                                oos.flush();  
                            }  
                        }else{  
                            Thread.sleep(10);  
                        }  
                    }   
// }  
            }   catch (Exception e) {  
                e.printStackTrace();  
                overThis();  
            }
        }  
          
        private void overThis() {  
            if(run)run=false;  
            if(s!=null){  
                try {  
                    s.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
            logger.warn("关闭："+s.getRemoteSocketAddress());  
        }  
          
    }  
    
    //////////////////////////////////////////////////////////////////////////////////////////
    private int nodeNum=0;
    private static int nodeCount=0;
    private long curView =0;
    private static long toView = 0;
    private static long reqView = 0;
    private static int failNodeCnt = 0;
    private static Map<Long,Set<ViewChange>> reqViewMap = new ConcurrentHashMap<>();
    private int timeOut =10000;
    private long lastConsensusTime = System.currentTimeMillis();
    static List<Node> allNodeList = new ArrayList<>();
    List<Long> viewChangeReq = new ArrayList<>();
    
    public boolean isLeader() {
    	return getLeader() == nodeNum ? true:false;
    }
    
    public int getLeader() {
    	return (int) (curView % nodeCount) ;
    }
      
   public void checkTimeout() {
	   new Thread(new Runnable() {
		
		@Override
		public void run() {
			 while(true) {
				   if(System.currentTimeMillis()-lastConsensusTime > timeOut) {
					   broadViewChangeReq();
				   }
				   else {
					   try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   }
			   }
			
		}
	}).start();
	  
   } 
   
    public void broadViewChangeReq() {
    	toView = curView+1;
    	sendToAllNode();
    	lastConsensusTime = System.currentTimeMillis();
    	checkAndChangeView();
    }
    
    	public void checkAndChangeView() {
    		if(nodeCount < 3) {
    			logger.info("not enough node to reach consensus");
    			return;
    		}
    		if(MessageAction.cnt > 0) {
    			if(reqViewMap.get(reqView).size() >= nodeCount - failNodeCnt - 1) {
    				toView = reqView;
    				curView = toView;
    				MessageAction.cnt = 0;
    				logger.info("######### Reach consensus, to_view=" + curView);
    				lastConsensusTime = System.currentTimeMillis();
    			}
    		}
    }
    
   
    public void sendToAllNode() {
      
    	for(Entry<Integer, Client> en: nodeMap.entrySet()) {
    		if(en.getKey() == self.getNodeNum()) continue;
    		Message msg = new Message();
    		msg.setMsgType("1");
    		ViewChange view = new ViewChange();
    		view.setFromHost(server.ip);
    		view.setFromPort(server.port);
    		view.setFormView(curView); 
    		view.setToView(toView);
    		msg.setBody(view);
    		en.getValue().sendObject(msg);
    	}
			//client2.sendObject(msg);
       // client.stop();
    }
    
    public static void addNode(Node node) {
    	if(allNodeList.contains(node)) {
    		return;
    	}
    	allNodeList.add(node);
    	Client client = new Client(node.getIp(),node.getPort());
    	try {
			client.start();
			Message msg = new Message();
    		msg.setMsgType("4");
    		Node n = new Node();
    		n.setIp(self.getIp());
    		n.setPort(self.getPort());
    		n.setNodeNum(self.getNodeNum());
    		logger.info("add node for ip:" + n.getIp() +" port:" + n.getPort()+" nodeNum:" +n.getNodeNum());
    		msg.setBody(n);
    		client.sendObject(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	nodeMap.put(node.getNodeNum(), client);
    	nodeCount = allNodeList.size();
    	failNodeCnt = (nodeCount -1) / 3;
    	logger.info("now system node count is:" + nodeCount + " can be fail node count is:" + failNodeCnt);
    }
    
    public List<Node>getAllNodes(){
    	return allNodeList;
    }
    
    public static Map getKey() {
    	   return keyMap;
    }
    
    public static final class MessageAction implements ObjectAction{

    	private static int cnt = 0; 
    	@Override
    	public Object doAction(Object rev) {
    		Message msg =(Message)rev;
    		switch (msg.getMsgType()) {
    		case "1":
    			ViewChange view = (ViewChange) msg.getBody();
    			if(view.getToView() >= toView) {
    				reqView = view.getToView();
    				Set<ViewChange> set  = reqViewMap.get(reqView);
    				if(set == null) {
    					set = new HashSet<>();
    					set.add(view);
    					reqViewMap.put(reqView, set);
    				}else {
    					set.add(view);
    					reqViewMap.put(reqView, set);
    				}
    				cnt++;
    			}
    			break;
    		case "2":
    		case "3":
    			try {
    				Server s = new Server(65432);
    				if(s.isLeader()) {
    					if("2".equals(msg.getMsgType())) {
    						
    						/*String res = SdkService.query(fcn, args, type);
    						msg.setBody(res);*/
    					}else {
    						/*String res = SdkService.invoke(fcn, args, type);
    						msg.setBody(res);*/
    					}
    				}else {
    					int nodeNum = s.getLeader();
    				}
    				
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			
    			break;
    		case "4":
    			Node node = (Node)msg.getBody();
    			if(allNodeList.contains(node)) {
    				return null;
    			}
    			allNodeList.add(node);
    			nodeCount = allNodeList.size();
    			failNodeCnt = (nodeCount -1) / 3;
    	    	logger.info("now system node count is:" + nodeCount + " can be fail node count is:" + failNodeCnt);

    			Client c =  new Client(node.getIp(),node.getPort());
    			try {
					c.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			nodeMap.put(node.getNodeNum(), c);
           	   	for(Node n : allNodeList) {
           	   		//if(n.getNodeNum() == self.getNodeNum())continue;
           	   		Client client = nodeMap.get(node.getNodeNum());
           	   		Message m =  new Message();
           	   		m.setMsgType("4");
           	   		m.setBody(n);
           	   		client.sendObject(m);
           	   	}
    			break;
    		default:
    			break;
    		}
    		return msg;
    	}
    	
    }
} 