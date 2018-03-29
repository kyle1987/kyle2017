package com.formssi.middleware.pbft;

import java.util.concurrent.CountDownLatch;

import com.formssi.middleware.encrypt.RSACoder;

public class ClientMessageAction implements Client.ObjectAction{

	public static int signCount  = 0;
	
	public static CountDownLatch latch;
	@Override
	public void doAction(Object obj, Client client) {
		Message msg =(Message)obj;
		switch (msg.getMsgType()) {
		case "1":
			try {
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(RSACoder.verify(msg.getBody().toString().getBytes(), RSACoder.getPublicKey(Server.getKey()), msg.getSign())) {
					latch.countDown();
					signCount++;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//latch.countDown();
			}
			
			break;

		default:
			break;
		}
		
	}

}
