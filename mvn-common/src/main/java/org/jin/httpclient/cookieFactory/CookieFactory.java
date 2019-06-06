package org.jin.httpclient.cookieFactory;


/**
 * 必须线程安全
 * 
 */
public interface CookieFactory extends CookieServiceConstants {
	
	
	

	/**
	 * 获取缓存cookie,优先从缓存取，其次从远程服务器取
	 * 
	 * @return
	 */
	public CookieInfo getCachedCookie();

	public void refreshCachedCookie();

}
