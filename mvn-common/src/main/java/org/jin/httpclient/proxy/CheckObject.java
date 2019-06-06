package org.jin.httpclient.proxy;

public class CheckObject {

	private String url;
	private String containObject;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContainObject() {
		return containObject;
	}
	public void setContainObject(String containObject) {
		this.containObject = containObject;
	}
	public CheckObject(String url, String containObject) {
		super();
		this.url = url;
		this.containObject = containObject;
	}
	
	
}
