package org.jin.httpclient;

import java.nio.charset.Charset;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.jin.httpclient.exception.FetchException;

public interface Client extends Request{

	/**
	 * @param url
	 * @return
	 * @throws FetchException
	 */
	public Response get(String url) throws FetchException;
	
	/**
	 * @param url
	 * @param headers
	 * @param charset
	 * @return
	 * @throws FetchException
	 */
	public Response get(String url, Map<String,String> headers, Charset charset) throws FetchException;
	
	/**
	 * @param url
	 * @param headers
	 * @param form
	 * @param charset
	 * @return
	 * @throws FetchException
	 */
	public Response post(String url, Map<String,String> headers, String form, Charset charset) throws FetchException;
	
	/**
	 * @param url
	 * @param headers
	 * @param form
	 * @param charset
	 * @return
	 * @throws FetchException
	 */
	public Response post(String url, Map<String,String> headers, Map<String,String> form, Charset charset) throws FetchException;
	
	/**
	 * @param url
	 * @param headers
	 * @param entity
	 * @return
	 * @throws FetchException
	 */
	public Response post(String url, Map<String,String> headers, HttpEntity entity) throws FetchException;

}
