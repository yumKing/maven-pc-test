package org.jin.httpclient.bytesAdapter;

import org.jin.httpclient.enums.DataType;
import org.jin.httpclient.utils.URLUtil;

public class UrlBA implements BytesAdapter {

	@Override
	public byte[] toBytes(Object value) {
		String url = (String) value;
		return DataType.STRING.toBytes(URLUtil.reverseUrl(url));
	}

	@Override
	public Object toObject(byte[] bytes) {
		String r = (String) DataType.STRING.toObject(bytes);
		return URLUtil.unreverseUrl(r);
	}

	@Override
	public Object toObject(byte[] bytes, int offset, int length) {
		String r = (String) DataType.STRING.toObject(bytes, offset, length);
		return URLUtil.unreverseUrl(r);
	}


}
