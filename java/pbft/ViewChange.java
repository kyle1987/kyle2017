package com.formssi.middleware.pbft;

import java.io.Serializable;
import java.util.Arrays;

public class ViewChange implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = 4299821851029126575L;
	private String fromHost ;
	private int fromPort ;
	
public String getFromHost() {
		return fromHost;
	}
	public void setFromHost(String fromHost) {
		this.fromHost = fromHost;
	}
	public int getFromPort() {
		return fromPort;
	}
	public void setFromPort(int fromPort) {
		this.fromPort = fromPort;
	}
private long formView;

private long toView;
public long getFormView() {
	return formView;
}
public void setFormView(long formView) {
	this.formView = formView;
}
public long getToView() {
	return toView;
}
public void setToView(long toView) {
	this.toView = toView;
}
@Override
public String toString() {
	return "ViewChange [fromHost=" + fromHost + ", fromPort=" + fromPort + ", formView=" + formView + ", toView="
			+ toView + "]";
}
@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (int) (formView ^ (formView >>> 32));
	result = prime * result + ((fromHost == null) ? 0 : fromHost.hashCode());
	result = prime * result + fromPort;
	result = prime * result + (int) (toView ^ (toView >>> 32));
	return result;
}
@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	ViewChange other = (ViewChange) obj;
	if (formView != other.formView)
		return false;
	if (fromHost == null) {
		if (other.fromHost != null)
			return false;
	} else if (!fromHost.equals(other.fromHost))
		return false;
	if (fromPort != other.fromPort)
		return false;
	if (toView != other.toView)
		return false;
	return true;
}

}
