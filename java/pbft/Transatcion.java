package com.formssi.middleware.pbft;

import java.io.Serializable;
import java.util.Arrays;

public class Transatcion implements Serializable{
	private static final long serialVersionUID = -260410480797483079L;
private String fcn;
private Object[] args;
public String getFcn() {
	return fcn;
}
public void setFcn(String fcn) {
	this.fcn = fcn;
}
public Object[] getArgs() {
	return args;
}
public void setArgs(Object[] args) {
	this.args = args;
}
public String getType() {
	return type;
}
public void setType(String type) {
	this.type = type;
}
private String type;

@Override
public String toString() {
	return "Transatcion [fcn=" + fcn + ", args=" + Arrays.toString(args) + ", type=" + type + "]";
}
}
