package org.jin.httpclient;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import org.jin.httpclient.annotation.HField;
import org.jin.httpclient.annotation.HRowField;
import org.jin.httpclient.bytesAdapter.ByteUtils;
import org.jin.httpclient.bytesAdapter.UrlBA;
import org.jin.httpclient.exception.FetchException;
import org.jin.httpclient.exception.RuntimeDaoException;
import org.jin.httpclient.io.UnSynByteArrayInputStream;
import org.jin.httpclient.utils.CloseUtils;
import org.jin.httpclient.utils.CommonUtils;

public class Response {

	public static final Response DEFAULT = new Response();

	private static final byte[] EMPTY = new byte[0];

	@HRowField(adapter = UrlBA.class)
	private String url;

	/**
	 * 数据二进制流
	 */
	@HField(q = "binary")
	private byte[] binary = EMPTY;
	/**
	 * content的压缩编码
	 */
	@HField(q = "contentEncoding")
	private String contentEncoding;

	@HField(q = "contentType")
	private String contentType;

	/** The fetch start time. */
	@HField(q = "fetchTime")
	private Date fetchTime;

	/** The cost time. */
	@HField(q = "cost")
	private long cost;

	@HField(q = "rate")
	private int rate;// 单位kb/s

	private HttpResponse httpResponse;

	/******************** results ***********************/
	private String html;

	private Document document;

	private JSONArray jsonarray;

	private JSONObject json;

	private InputStream is;

	public String getUrl() {
		return url;
	}

	public Response() {
	}

	public Response(byte[] binary) {
		this.binary = binary;
	}

	public String asHtml() {
		if (html == null) {
			try {
				String charset = getContentCharSet();
				if (charset != null) {
					html = new String(binary, Charset.forName(charset));
				} else {
					CharsetMatch match = new CharsetDetector().setText(binary).detect();
					html = match.getString();
				}
			} catch (Exception e) {
				throw new RuntimeDaoException(e);
			}
		}
		return html;
	}

	private String getContentCharSet() throws ParseException {
		String charset = null;
		if (CommonUtils.isNotEmpty(contentType)) {
			String[] strs = contentType.split(";");
			for (String string : strs) {
				if (string.contains("charset")) {
					String[] tmp = string.split("=");
					if (tmp.length == 2) {
						return tmp[1];
					}
				}
			}
		}
		return charset;
	}

	public String asHtml(Charset c) {
		if (html == null) {
			html = new String(binary, c);
		}
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
		this.binary = ByteUtils.toBytes(html);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Response other = (Response) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	public Date getFetchTime() {
		return fetchTime;
	}

	public void setFetchTime(Date fetchTime) {
		this.fetchTime = fetchTime;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public byte[] getBinary() {
		return binary;
	}

	public void setBinary(byte[] binary) {
		this.binary = binary;
	}

	public long getCost() {
		return cost;
	}

	public void setCost(long cost) {
		this.cost = cost;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public org.jsoup.nodes.Document asDocument() {
		if (document == null) {
			document = asHtml() == null ? null : Jsoup.parse(asHtml(), url);
		}
		return document;
	}

	public InputStream asInputStream() {
		if (is == null) {
			is = new UnSynByteArrayInputStream(binary);
		}
		return is;
	}

	public JSONObject asJSONObject() {
		if (json == null) {
			json = JSON.parseObject(asHtml());
		}
		return json;
	}

	public JSONArray asJSONArray() {
		if (jsonarray == null) {
			jsonarray = JSON.parseArray(asHtml());
		}
		return jsonarray;
	}

	public static Response connect(String url) throws FetchException {
		Client getter = new InnerJavaClient();
		try {
			return getter.get(url);
		} catch (FetchException e) {
			throw e;
		} finally {
			CloseUtils.close(getter);
		}
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	@Override
	public String toString() {
		return "Response [url=" + url + ", binary=" + binary + ", contentEncoding=" + contentEncoding + ", contentType=" + contentType + ", fetchTime=" + fetchTime + ", cost=" + cost + ", rate=" + rate + "]";
	}
	
}
