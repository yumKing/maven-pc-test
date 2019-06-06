package org.jin.httpclient.cookieFactory;

import org.jin.httpclient.utils.CommonUtils;

public interface CookieServiceConstants {

	public static final String SINA = "sina";
	public static final String SINA_COOKIE_QUEUE = "SINA_COOKIE_QUEUE";
	public static final String QQ = "qq";
	public static final String QQ_COOKIE_QUEUE = "QQ_COOKIE_QUEUE";
	public static final String ERROR = "no access cookie";
	public static long LIVINGTIME = 5 * 24 * 3600 * 1000l;// cookie存活时间
	public static long USECACHETIME = 2 * 3600 * 1000l;
	public static long REUSERTIME = 4 * 3600 * 1000l;
	public static final CookieInfo DEFAULTCOOKIE = new CookieInfo(ERROR,
			"test", "test");
	public static final User SINA_DEFAULT = new User("test@test.com",
			"test");
	public static final String IP = CommonUtils.getIP();
	
}
