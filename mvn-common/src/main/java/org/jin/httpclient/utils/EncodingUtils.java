package org.jin.httpclient.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.jin.httpclient.io.UnSynByteArrayInputStream;
import org.jin.httpclient.io.UnSynByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EncodingUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(EncodingUtils.class);

	private static final int EXPECTED_GZIP_COMPRESSION_RATIO = 5;
	private static final int EXPECTED_DEFLATE_COMPRESSION_RATIO = 5;
	private static final int BUF_SIZE = 4096;

	public static class ExpandedResult {
		private byte[] _expanded;
		private boolean _isTruncated;

		public ExpandedResult(byte[] expanded, boolean isTruncated) {
			super();
			_expanded = expanded;
			_isTruncated = isTruncated;
		}

		public byte[] getExpanded() {
			return _expanded;
		}

		public void setExpanded(byte[] expanded) {
			_expanded = expanded;
		}

		public boolean isTruncated() {
			return _isTruncated;
		}

		public void setTruncated(boolean isTruncated) {
			_isTruncated = isTruncated;
		}
	}

	public static byte[] processGzipEncoded(byte[] compressed)
			throws IOException {
		return processGzipEncoded(compressed, Integer.MAX_VALUE).getExpanded();
	}
	
	/**
	 * 采用gzip算法压缩
	 * @param bytes
	 * @return
	 * @throws java.io.IOException
	 */
	public static byte[] gzipEncode(byte[] bytes)
			throws IOException {
		UnSynByteArrayOutputStream ubo = new UnSynByteArrayOutputStream();
		GZIPOutputStream out = new GZIPOutputStream(ubo);
		out.write(bytes);
		out.flush();
		out.close();
		return ubo.toByteArray();
	}

	public static ExpandedResult processGzipEncoded(byte[] compressed,
			int sizeLimit) throws IOException {

		UnSynByteArrayOutputStream outStream = new UnSynByteArrayOutputStream(
				EXPECTED_GZIP_COMPRESSION_RATIO * compressed.length);
		GZIPInputStream inStream = new GZIPInputStream(
				new UnSynByteArrayInputStream(compressed));

		boolean isTruncated = false;
		byte[] buf = new byte[BUF_SIZE];
		int written = 0;
		while (true) {
			try {
				int size = inStream.read(buf);
				if (size <= 0) {
					break;
				}

				if ((written + size) > sizeLimit) {
					isTruncated = true;
					outStream.write(buf, 0, sizeLimit - written);
					break;
				}

				outStream.write(buf, 0, size);
				written += size;
			} catch (Exception e) {
				LOGGER.trace("Exception unzipping content", e);
				break;
			}
		}

		CloseUtils.close(outStream);
		CloseUtils.close(inStream);
		return new ExpandedResult(outStream.toByteArray(), isTruncated);
	}

	// TODO KKr The following routines are designed to support the deflate
	// compression standard (RFC 1250) for HTTP 1.1 (RFC 2616). However,
	// I was unable to verify that they really work correctly, so I've
	// removed deflate from SimpleHttpFetcher.DEFAULT_ACCEPT_ENCODING.

	public static byte[] processDeflateEncoded(byte[] content)
			throws IOException {
		return processDeflateEncoded(content, Integer.MAX_VALUE);
	}

	public static byte[] processDeflateEncoded(byte[] compressed, int sizeLimit)
			throws IOException {
		UnSynByteArrayOutputStream outStream = new UnSynByteArrayOutputStream(
				EXPECTED_DEFLATE_COMPRESSION_RATIO * compressed.length);

		// "true" because HTTP does not provide zlib headers
		Inflater inflater = new Inflater(true);
		InflaterInputStream inStream = new InflaterInputStream(
				new ByteArrayInputStream(compressed), inflater);

		byte[] buf = new byte[BUF_SIZE];
		int written = 0;
		while (true) {
			try {
				int size = inStream.read(buf);
				if (size <= 0) {
					break;
				}

				if ((written + size) > sizeLimit) {
					outStream.write(buf, 0, sizeLimit - written);
					break;
				}

				outStream.write(buf, 0, size);
				written += size;
			} catch (Exception e) {
				LOGGER.trace("Exception inflating content", e);
				break;
			}
		}

		CloseUtils.close(outStream);
		return outStream.toByteArray();
	}

}
