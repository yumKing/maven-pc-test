package org.jin.httpclient.utils;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.regex.Pattern;


/** Utility class for URL analysis */
public class URLUtil {
	public static final Charset UTF8 = Charset.forName("utf8");
	public static final Charset GBK = Charset.forName("gbk");
	public static final String BLANK = new String(new char[] { (char) 160 });

	public static final Pattern IP_PATTERN = Pattern.compile("(\\d{1,3}\\.){3}(\\d{1,3})");

	public static final Pattern IP_PORT_PATTERN = Pattern.compile("(\\d{1,3}\\.){3}(\\d{1,3}):(\\d{1,5})");

	/**
	 * &nbsp
	 */
	public static final String NBSP = new String(new char[] { (char) 160 });

	/** Partitions of the hostname of the url by "." */
	public static String[] getHostBatches(URL url) {
		String host = url.getHost();
		// return whole hostname, if it is an ipv4
		// TODO : handle ipv6
		if (IP_PATTERN.matcher(host).matches())
			return new String[] { host };
		return host.split("\\.");
	}

	public static String encodeUTF8(String url) {
		try {
			return URLEncoder.encode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}

	public static String encode(String s, String enc) {
		try {
			return URLEncoder.encode(s, enc);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static String encodeGBK(String url) {
		try {
			return URLEncoder.encode(url, "gbk");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}

	public static String decodeUTF8(String url) {
		try {
			return URLDecoder.decode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}

	public static String decodeGBK(String url) {
		try {
			return URLDecoder.decode(url, "gbk");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}

	public static String decode(String s, String enc) {
		try {
			return URLDecoder.decode(s, enc);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * Partitions of the hostname of the url by "."
	 * 
	 * @throws java.net.MalformedURLException
	 */
	public static String[] getHostBatches(String url) throws MalformedURLException {
		return getHostBatches(new URL(url));
	}

	/**
	 * Returns the lowercased hostname for the url or null if the url is not
	 * well formed.
	 *
	 * @param url
	 *            The url to check.
	 * @return String The hostname for the url.
	 */
	public static String getHost(String url) {
		try {
			return new URL(url).getHost().toLowerCase();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Returns the page for the url. The page consists of the protocol, host,
	 * and path, but does not include the query string. The host is lowercased
	 * but the path is not.
	 *
	 * @param url
	 *            The url to check.
	 * @return String The page for the url.
	 */
	public static String getPage(String url) {
		try {
			// get the full url, and replace the query string with and empty
			// string
			url = url.toLowerCase();
			String queryStr = new URL(url).getQuery();
			return (queryStr != null) ? url.replace("?" + queryStr, "") : url;
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static String toASCII(String url) {
		try {
			URL u = new URL(url);
			URI p = new URI(u.getProtocol(), null, IDN.toASCII(u.getHost()), u.getPort(), u.getPath(), u.getQuery(),
					u.getRef());

			return p.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static String toUNICODE(String url) {
		try {
			URL u = new URL(url);
			URI p = new URI(u.getProtocol(), null, IDN.toUnicode(u.getHost()), u.getPort(), u.getPath(), u.getQuery(),
					u.getRef());

			return p.toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 将相对url转化为绝对url
	 *
	 * @param url
	 * @param relativePath
	 * @return
	 */
	public static String toAbsoluteUrl(String url, String relativePath) {
		try {
			return new URL(new URL(url), relativePath).toString();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static String toAbsoluteUrl(URL url, String relativePath) {
		// another way
		// java.net.URI anURI=url.toURI();
		// URI resultURI=anURI.resolve(relativePath);
		// System.out.println(resultURI);
		try {
			return new URL(url, relativePath).toString();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * 将127.0.0.1形式的IP地址转换成十进制整数
	 *
	 * @param strIp
	 * @return
	 */
	public static int ipToInt(String strIp) {
		int[] ip = new int[4];
		// 先找到IP地址字符串中.的位置
		int position1 = strIp.indexOf(".");
		int position2 = strIp.indexOf(".", position1 + 1);
		int position3 = strIp.indexOf(".", position2 + 1);
		// 将每个.之间的字符串转换成整型
		ip[0] = Integer.parseInt(strIp.substring(0, position1));
		ip[1] = Integer.parseInt(strIp.substring(position1 + 1, position2));
		ip[2] = Integer.parseInt(strIp.substring(position2 + 1, position3));
		ip[3] = Integer.parseInt(strIp.substring(position3 + 1));
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
	}

	/**
	 * 将十进制整数形式转换成127.0.0.1形式的ip地址
	 *
	 * @param intIp
	 * @return
	 */
	public static String intToIP(int intIp) {
		StringBuilder sb = new StringBuilder();
		// 直接右移24位
		sb.append(String.valueOf((intIp >>> 24)));
		sb.append(".");
		// 将高8位置0，然后右移16位
		sb.append(String.valueOf((intIp & 0x00FFFFFF) >>> 16));
		sb.append(".");
		// 将高16位置0，然后右移8位
		sb.append(String.valueOf((intIp & 0x0000FFFF) >>> 8));
		sb.append(".");
		// 将高24位置0
		sb.append(String.valueOf((intIp & 0x000000FF)));
		return sb.toString();
	}

	/**
	 * Reverses a url's domain. This form is better for storing in hbase.
	 * Because scans within the same domain are faster.
	 * <p>
	 * E.g. "http://bar.foo.com:8983/to/index.html?a=b" becomes
	 * "com.foo.bar:8983:http/to/index.html?a=b".
	 *
	 * @param url
	 *            url to be reversed
	 * @return Reversed url
	 * @throws java.net.MalformedURLException
	 */
	public static String reverseUrl(String urlString) {
		try {
			return reverseUrl(new URL(urlString));
		} catch (MalformedURLException e) {
			return urlString;
		}
	}

	/**
	 * Reverses a url's domain. This form is better for storing in hbase.
	 * Because scans within the same domain are faster.
	 * <p>
	 * E.g. "http://bar.foo.com:8983/to/index.html?a=b" becomes
	 * "com.foo.bar:http:8983/to/index.html?a=b".
	 * 
	 * @param url
	 *            url to be reversed
	 * @return Reversed url
	 */
	public static String reverseUrl(URL url) {
		String host = url.getHost();
		String file = url.getFile();
		String protocol = url.getProtocol();
		int port = url.getPort();

		StringBuilder buf = new StringBuilder();

		/* reverse host */
		reverseAppendSplits(host, buf);

		/* add protocol */
		buf.append(':');
		buf.append(protocol);

		/* add port if necessary */
		if (port != -1) {
			buf.append(':');
			buf.append(port);
		}

		/* add path */
		if (file.length() > 0 && '/' != file.charAt(0)) {
			buf.append('/');
		}
		buf.append(file);

		return buf.toString();
	}

	public static String unreverseUrl(String reversedUrl) {
		StringBuilder buf = new StringBuilder(reversedUrl.length() + 2);

		int pathBegin = reversedUrl.indexOf('/');
		if (pathBegin == -1)
			pathBegin = reversedUrl.length();
		String sub = reversedUrl.substring(0, pathBegin);

		String[] splits = CommonUtils.splitPreserveAllTokens(sub, ':'); // {<reversed
																		// host>,
																		// <port>,
																		// <protocol>}

		buf.append(splits[1]); // add protocol
		buf.append("://");
		reverseAppendSplits(splits[0], buf); // splits[0] is reversed
		// host
		if (splits.length == 3) { // has a port
			buf.append(':');
			buf.append(splits[2]);
		}
		buf.append(reversedUrl.substring(pathBegin));
		return buf.toString();
	}

	/**
	 * Given a reversed url, returns the reversed host E.g
	 * "com.foo.bar:http:8983/to/index.html?a=b" -> "com.foo.bar"
	 * 
	 * @param reversedUrl
	 *            Reversed url
	 * @return Reversed host
	 */
	public static String getReversedHost(String reversedUrl) {
		return reversedUrl.substring(0, reversedUrl.indexOf(':'));
	}

	private static void reverseAppendSplits(String string, StringBuilder buf) {
		String[] splits = CommonUtils.split(string, '.');
		if (splits.length > 0) {
			for (int i = splits.length - 1; i > 0; i--) {
				buf.append(splits[i]);
				buf.append('.');
			}
			buf.append(splits[0]);
		} else {
			buf.append(string);
		}
	}

	public static String reverseHost(String hostName) {
		StringBuilder buf = new StringBuilder();
		reverseAppendSplits(hostName, buf);
		return buf.toString();

	}

	public static String unreverseHost(String reversedHostName) {
		return reverseHost(reversedHostName); // Reversible
	}

	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		String ipStr = "192.168.0.1";
		int longIp = ipToInt(ipStr);
		System.out.println("192.168.0.1 的整数形式为：" + longIp);
		System.out.println("整数" + longIp + "转化成字符串IP地址：" + intToIP(longIp));
		// ip地址转化成二进制形式输出
		System.out.println("192.168.0.1 的二进制形式为：" + Long.toBinaryString(longIp));
		URL url = new URL("http://www.abc.com/aa/bb/cc/file.html");
		String relativePath = "../file2.html";
		System.out.println(toAbsoluteUrl(url, relativePath));

		url = new URL("http://tieba.baidu.com/f?kw=exo");
		relativePath = "/p/3031186029";
		System.out.println(toAbsoluteUrl(url, relativePath));
		System.out.println(toAbsoluteUrl(url, relativePath));

		System.out.println(reverseUrl(url));
		System.out.println(unreverseUrl(reverseUrl(url)));
	}

}
