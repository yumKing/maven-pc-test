package org.jin.httpclient.cookieFactory;

import org.jin.httpclient.utils.CommonUtils;

public class DefaultCookieFactory implements CookieFactory {

	private CookieInfo cookie;

	public DefaultCookieFactory(String cookie) {
		super();
		setCookie(cookie);
	}

	public void setCookie(String cookie) {
		if (CommonUtils.isEmpty(cookie)) {
			throw new IllegalArgumentException("cookie is empty!");
		}
		this.cookie = new CookieInfo(cookie, "single", "single");
	}

	@Override
	public CookieInfo getCachedCookie() {
		return cookie;
	}

	@Override
	public void refreshCachedCookie() {
		
	}

	

}
