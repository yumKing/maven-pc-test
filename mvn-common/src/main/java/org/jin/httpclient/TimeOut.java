package org.jin.httpclient;

public class TimeOut {
	private int connectRequestTimeout;
	private int connectTimeout;
	private int socketTimeout;
	public TimeOut(int connectRequestTimeout, int connectTimeout, int socketTimeout) {
		super();
		this.connectRequestTimeout = connectRequestTimeout;
		this.connectTimeout = connectTimeout;
		this.socketTimeout = socketTimeout;
	}
	public int getConnectRequestTimeout() {
		return connectRequestTimeout;
	}
	public void setConnectRequestTimeout(int connectRequestTimeout) {
		this.connectRequestTimeout = connectRequestTimeout;
	}
	public int getConnectTimeout() {
		return connectTimeout;
	}
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	public int getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	
}
