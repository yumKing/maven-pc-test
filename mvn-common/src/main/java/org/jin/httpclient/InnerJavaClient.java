package org.jin.httpclient;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;

import org.jin.httpclient.exception.FetchException;


public class InnerJavaClient extends AbstractRequest{
	static CookieManager manager;
	static {
		manager = new CookieManager();
		CookieHandler.setDefault(manager);
	}

	@Override
	public Response get(String url, Map<String, String> headers, Charset charset) throws FetchException {
		HttpURLConnection con = null;
		try {
			URL u = new URL(url);
			URLConnection rulConnection = u.openConnection();
			TimeOut t = timeout.get();
			if (t == DEFAULT_TIMEOUNT) {
				t = PROXY_TIMEOUNT;
			}
			rulConnection.setReadTimeout(t.getConnectRequestTimeout());
			rulConnection.setConnectTimeout(t.getConnectTimeout());
			con = (HttpURLConnection) rulConnection;
			con.setRequestMethod("GET");
			if (headers != null) {
				for (Entry<String, String> entry : headers.entrySet()) {
					con.addRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			return Entitys.toResponse(url, con);
		} catch (Throwable e) {
			throw new FetchException(url, e);
		} finally {
			con.disconnect();
//			CookieStore cookieJar = manager.getCookieStore();
//			List<HttpCookie> cookies = cookieJar.getCookies();
//			for (HttpCookie cookie : cookies) {
//				System.out.println(cookie);
//			}
		}
	}

	@Override
	public Response post(String url, Map<String, String> headers, HttpEntity form) throws FetchException {
		HttpURLConnection con = null;
		try {
			URL u = new URL(url);
			URLConnection rulConnection = u.openConnection();
			TimeOut t = timeout.get();
			if (t == DEFAULT_TIMEOUNT) {
				t = PROXY_TIMEOUNT;
			}
			rulConnection.setReadTimeout(t.getConnectRequestTimeout());
			rulConnection.setConnectTimeout(t.getConnectTimeout());
			con = (HttpURLConnection) rulConnection;
			con.setRequestMethod("POST");
			if (headers != null) {
				for (Entry<String, String> entry : headers.entrySet()) {
					con.addRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			con.setDoOutput(true);
			con.getOutputStream().write(IOUtils.toByteArray(form.getContent()));
			con.getOutputStream().flush();
			con.getOutputStream().close();
			return Entitys.toResponse(url, con);
		} catch (Throwable e) {
			throw new FetchException(url, e);
		} finally {
			if (con != null)
				con.disconnect();
		}
	}

	@Override
	public void clearCookies() {

	}

	@Override
	public void close() throws Exception {

	}

}
