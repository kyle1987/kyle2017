package com.formssi.middleware.pbft;

import com.formssi.middleware.encrypt.RSACoder;

public class MessageAction implements Server.ObjectAction{

	private volatile boolean flag =false;
	@Override
	public Object doAction(Object rev) {
		Message msg =(Message)rev;
		String signData = null ;
		switch (msg.getMsgType()) {
		case "1":
			ViewChange view = (ViewChange) msg.getBody();
			if(isExistViewChange()) {
				return null;
			}
			view.getToView();
			try {
				signData = RSACoder.sign(msg.getBody().toString().getBytes(), RSACoder.getPrivateKey(Server.getKey()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			msg.setSign(signData);
			
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

		default:
			break;
		}
		return msg;
	}
	
	public boolean isExistViewChange() {
		return flag;
	}
}
