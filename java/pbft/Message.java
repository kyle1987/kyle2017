package com.formssi.middleware.pbft;

import java.io.Serializable;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3242101971280383943L;
	String msgType;//1-视图改变 2-交易 3-增加节点
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public Object getBody() {
		return body;
	}
	public void setBody(Object body) {
		this.body = body;
	}
	Object body;
	String sign;
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	@Override
	public String toString() {
		return "Message [msgType=" + msgType + ", body=" + body + ", sign=" + sign + "]";
	}
}
