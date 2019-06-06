package org.jin.httpclient.cookieFactory;

import org.jin.httpclient.annotation.ProtobufField;

public class CookieInfo implements Comparable<CookieInfo>{

	@ProtobufField(index = 1)
	private String cookie; 

	@ProtobufField(index = 2)
	private String username; 

	@ProtobufField(index = 3)
	private String password; 

	public CookieInfo() {
		super();
	}
	
	

	public CookieInfo(String cookie, String username, String password) {
		super();
		this.cookie = cookie;
		this.username = username;
		this.password = password;
	}



	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cookie == null) ? 0 : cookie.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		CookieInfo other = (CookieInfo) obj;
		if (cookie == null) {
			if (other.cookie != null)
				return false;
		} else if (!cookie.equals(other.cookie))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public int compareTo(CookieInfo o) {
		return cookie.compareTo(o.cookie);
	}


}