package org.jin.httpclient;

import org.apache.http.HttpHost;

import org.jin.httpclient.enums.UserAgent;

/**
 * AutoCloseable 实现了该接口，就可以在try(block)块中，
 * 加入该对象，该对象就会自动关闭资源，不需要finally中手动关闭
 * @author jinyang
 *
 */
public interface Request extends AutoCloseable{
	
//	public Cookie cookie = CookieFactory.getCookie();
//	public final TimeOut DEFAULT_TIMEOUT = new TimeOut(3000, 3000, 3000);
//	public final TimeOut PROXY_TIMEOUT = new TimeOut(5000, 5000, 5000);
//	public RetryHandler retry;
//	public UserAgent ua;
//	
//	public Proxy proxy;
	public static final TimeOut DEFAULT_TIMEOUNT = new TimeOut(2500, 2500, 2500);
	public static final TimeOut PROXY_TIMEOUNT = new TimeOut(5000, 5000, 5000);
	public static final String USE_SYSTEM_PROXY = "USE_SYSTEM_PROXY";
	
	public Request setTimeOut(TimeOut timeout);
	public Request setRetryCount(int count);
	public Request setUserAgent(UserAgent ua);
	public Request setProxy(HttpHost proxy);
	public void clearCookies();
}
