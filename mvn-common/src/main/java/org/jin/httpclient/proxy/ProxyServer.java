package org.jin.httpclient.proxy;

import org.apache.http.HttpHost;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import org.jin.httpclient.bytesAdapter.ByteUtils;

public class ProxyServer {
	public String host;
	public int port;
	public String requestType;
	public String location;

	public int priority;

	@JSONField(serialize = false, deserialize = false)
	public volatile boolean needCheck = false;
	

	public ProxyServer() {
		super();
	}

	public ProxyServer(String host, int port, String requestType, String location) {
		super();
		this.host = host;
		this.port = port;
		this.requestType = requestType;
		this.location = location;
	}


	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
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
		ProxyServer other = (ProxyServer) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	public HttpHost toHttpHost() {
		return new HttpHost(host, port);
	}

	public byte[] toKey() {
		return ByteUtils.toBytes(host + ":" + host);
	}
	
	public String toKeyString() {
		return host + ":" + host;
	}
	
	public byte[] toValue() {
		return ByteUtils.toBytes(toString());
	}

	public static ProxyServer fromValue(byte[] value) {
		String v = ByteUtils.toString(value);
		return JSONObject.parseObject(v, ProxyServer.class);
	}

}
