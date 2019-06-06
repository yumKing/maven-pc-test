package org.jin.httpclient.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.HttpHost;

import org.jin.httpclient.Client;
import org.jin.httpclient.DefaultClient;
import org.jin.httpclient.Request;
import org.jin.httpclient.Response;
import org.jin.httpclient.TimeOut;


public class ProxyChecker {
	private static Logger logger = Logger.getLogger("ProxyChecker");
	private Client getter;
	private long perfect_cost = 55;;
	private List<CheckObject> checkObjects = new ArrayList<CheckObject>();

	public ProxyChecker() {
		this(Request.PROXY_TIMEOUNT);
	}

	public ProxyChecker(final TimeOut timeout) {
		getter = new DefaultClient();
		getter.setTimeOut(timeout);
		perfect_cost = 55;
		getCheckObjects().add(new CheckObject("http://www.baidu.com/", "百度一下，你就知道"));
	}

	/**
	 * true 可用
	 * 
	 * @param proxyServer
	 * @return
	 */
	public boolean check(final ProxyServer proxyServer) {
		try {
			long start = System.currentTimeMillis();
			HttpHost proxy = new HttpHost(proxyServer.host, proxyServer.port);
			getter.setProxy(proxy);
			for (CheckObject checkObject : getCheckObjects()) {
				Response res = getter.get(checkObject.getUrl());
				boolean r = res.asHtml().contains(checkObject.getContainObject());
				if (!r) {
					return false;
				}
			}
			proxyServer.priority = (int) (perfect_cost * 100 / (System.currentTimeMillis() - start)) / getCheckObjects().size();
			return true;
		} catch (Exception e) {
			return false;
		}finally{
			getter.clearCookies();
		}

	}

	public boolean check(final HttpHost proxy) {
		try {
			getter.setProxy(proxy);
			for (CheckObject checkObject : getCheckObjects()) {
				Response res = getter.get(checkObject.getUrl());
				boolean r = res.asHtml().contains(checkObject.getContainObject());
				if (!r) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] args) {
		ProxyChecker checker = new ProxyChecker();
		System.setProperty(Request.USE_SYSTEM_PROXY, "true");
		boolean b = checker.check(new ProxyServer("117.59.217.228", 80, null, null));
		System.out.println(b);
	}

	public List<CheckObject> getCheckObjects() {
		return checkObjects;
	}

	public void setCheckObjects(List<CheckObject> checkObjects) {
		this.checkObjects = checkObjects;
	}

}