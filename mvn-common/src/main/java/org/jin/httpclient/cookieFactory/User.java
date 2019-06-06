package org.jin.httpclient.cookieFactory;

import com.alibaba.fastjson.JSONObject;

import org.jin.httpclient.annotation.ProtobufField;

public class User {
	@ProtobufField(index = 1)
	private String usename;
	@ProtobufField(index = 2)
	private String password;

	public User(String usename, String password) {
		super();
		this.setUsename(usename.trim());
		this.setPassword(password.trim());
	}
	
	

	public User() {
		super();
	}



	public String getUsename() {
		return usename;
	}

	public void setUsename(String usename) {
		this.usename = usename;
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
		result = prime * result + ((usename == null) ? 0 : usename.hashCode());
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
		User other = (User) obj;
		if (usename == null) {
			if (other.usename != null)
				return false;
		} else if (!usename.equals(other.usename))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return toJson();
	}

	public String toJson() {
		JSONObject res = new JSONObject();
		res.put("user", usename);
		res.put("password", password);
		return res.toString();
	}

}
