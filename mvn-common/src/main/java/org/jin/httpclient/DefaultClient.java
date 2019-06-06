package org.jin.httpclient;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import org.jin.httpclient.cookieFactory.CookieFactory;
import org.jin.httpclient.exception.FetchException;


public class DefaultClient extends AbstractHttpClient{
	
	public DefaultClient() {
		super();
	}

	public DefaultClient(CookieFactory cookieFactory) {
		super();
	}

	@Override
	public Response get(String url, Map<String, String> parameters, Charset charset) throws FetchException {
		HttpGet get = null;
		try {
			get = new HttpGet(URI.create(url));

			if (parameters != null) {
				for (Entry<String, String> entry : parameters.entrySet()) {
					get.addHeader(entry.getKey(), entry.getValue());
				}
			}
		} catch (Exception e) {
			throw new FetchException("RUBBISHURL_" + url, FetchException.RUBBISH);
		}
		return execute(get, url);
	}

	@Override
	public Response post(String url, Map<String, String> parameters, HttpEntity form) throws FetchException {
		HttpPost post = null;
		try {
			post = new HttpPost(URI.create(url));
			if (parameters != null) {
				for (Entry<String, String> entry : parameters.entrySet()) {
					post.addHeader(entry.getKey(), entry.getValue());
				}
			}
			if (form != null) {
				post.setEntity(form);
			}
		} catch (Exception e) {
			throw new FetchException("RUBBISHURL_" + url, FetchException.RUBBISH);
		}
		return execute(post, url);
	}
}
