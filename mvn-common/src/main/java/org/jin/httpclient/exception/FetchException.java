package org.jin.httpclient.exception;

import org.apache.http.HttpResponse;

public class FetchException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * type
	 */
	public static final int DEFAULT = 0;

	/**
	 * 垃圾,一般不能获取页面的抛错都是
	 */
	public static final int RUBBISH = 1;

	/**
	 * 
	 */
	public static final int IMPORTANT = 2;

	/**
	 * 不是期望的结果
	 */
	public static final int UNEXPECTED = 3;

	/**
	 * 编码问题
	 */
	public static final int CODING = 4;

	private int errortype = -1;
	private transient HttpResponse httpResponse;

	public FetchException() {
		super();
	}

	public FetchException(String message, Throwable cause) {
		super(message, cause);
	}

	public FetchException(String message) {
		super(message);
	}

	public FetchException(Throwable cause) {
		super(cause);
	}

	public FetchException(String message, int errortype) {
		super(message);
		this.errortype = errortype;
	}

	public FetchException(String message, int errortype, HttpResponse httpResponse) {
		super(message);
		this.errortype = errortype;
		this.setHttpResponse(httpResponse);
	}

	public int getErrortype() {
		return errortype;
	}

	public void setErrortype(int errortype) {
		this.errortype = errortype;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public int getCode() {
		return httpResponse != null ? httpResponse.getStatusLine().getStatusCode() : -1;
	}

}
