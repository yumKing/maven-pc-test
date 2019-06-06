package org.jin.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.Args;
import org.apache.http.util.ByteArrayBuffer;

import org.jin.httpclient.enums.AbortedFetchReason;
import org.jin.httpclient.exception.AbortedFetchException;
import org.jin.httpclient.exception.FetchException;
import org.jin.httpclient.utils.CloseUtils;
import org.jin.httpclient.utils.CommonUtils;
import org.jin.httpclient.utils.EncodingUtils;
import org.jin.httpclient.utils.EncodingUtils.ExpandedResult;

final class Entitys {

	private static int minResponseRate = 0;// 最小下载速度
	private static int maxContentSize = 5 * 1024 * 1024;// 5MB
	private static int actualMaxContentSize = maxContentSize * 5;// 25MB

	private Entitys() {
	}

	public static Response toResponse(final HttpRequestBase requestBase, final HttpResponse httpResponse, final Response res) throws IOException, FetchException {
		res.setHttpResponse(httpResponse);
		res.setFetchTime(new Date());
		int code = httpResponse.getStatusLine().getStatusCode();
		if (code == 521) {
			return res;
		}
		if (code > 299 && code < 400) {// 对于302、304,没必要读取了
			return res;
		}
		if (code >= 400 && code < 500) {// 404不是期望的结果
			throw new FetchException("code isn't legal,it's " + code, FetchException.UNEXPECTED, httpResponse);
		}
		if (code == 502) {// 对于其他服务器端错误都是很严重的
			throw new FetchException("code isn't legal,it's " + code, FetchException.IMPORTANT, httpResponse);
		}
		if (code < 200 || code >= 500) {// 对于其他服务器端错误都是很严重的
			throw new FetchException("code isn't legal,it's " + code, FetchException.DEFAULT, httpResponse);
		}

		// checkMimeType(requestBase, httpResponse.getEntity(), res);
		read(requestBase, httpResponse.getEntity(), res);
		return res;
	}

	public static void read(final HttpRequestBase requestBase, final HttpEntity entity, Response res) throws IOException, FetchException {
		read0(requestBase, entity, res);
		byte[] bytes = decode(res, entity);
		res.setBinary(bytes);
		res.setContentType(getContentType(entity));
	}

	/**
	 * 不接受经过解压缩包装的entity，因为那样子做，下面的下载速率统计就不准确了
	 * 
	 * @param entity
	 * @param res
	 * @throws java.io.IOException
	 * @throws com.timeless.http.FetchException
	 */
	private static void read0(final HttpRequestBase requestBase, final HttpEntity entity, Response res) throws IOException, FetchException {
		Args.notNull(entity, "Entity");
		final InputStream instream = entity.getContent();
		if (instream == null) {
			return;
		}
		boolean needAbort = true;
		try {
			Args.check(entity.getContentLength() <= Integer.MAX_VALUE, "HTTP entity too large to be buffered in memory");
			Args.check(!(entity instanceof GzipDecompressingEntity || entity instanceof DeflateDecompressingEntity), "entity cannot be Decompressing Wrappered!!!");
			int i = (int) entity.getContentLength();
			boolean truncated = false;
			if (i < 0) {
				i = 4096;
			} else {
				truncated = i > maxContentSize;
			}
			final ByteArrayBuffer buffer = new ByteArrayBuffer(i);
			final byte[] tmp = new byte[8192];
			int bytesRead = 0;
			int totalRead = 0;
			long readStartTime = res.getFetchTime().getTime();
			long readRate = 0;
			int readRequests = 0;// 读取的次数

			while ((bytesRead = instream.read(tmp)) != -1) {
				readRequests += 1;
				buffer.append(tmp, 0, bytesRead);
				totalRead += bytesRead;

				// Assume read time is at least one millisecond, to avoid DBZ
				// exception.
				long totalReadTime = Math.max(1, System.currentTimeMillis() - readStartTime);
				readRate = (totalRead * 1000L) / totalReadTime;

				// Don't bail on the first read cycle, as we can get a hiccup
				// starting out.
				// Also don't bail if we've read everything we need.
				if ((readRequests > 1) && (totalRead < maxContentSize) && (readRate < minResponseRate)) {
					throw new AbortedFetchException("Slow response rate of " + readRate + " KB/s", AbortedFetchReason.SLOW_RESPONSE_RATE);
				}
				// Check to see if we got interrupted, but don't clear the
				// interrupted flag.
				if (Thread.currentThread().isInterrupted()) {
					throw new AbortedFetchException(AbortedFetchReason.INTERRUPTED);
				}
			}
			needAbort = truncated || (instream.available() > 0);
			res.setBinary(buffer.toByteArray());
			res.setRate((int) (readRate / 1024));
			res.setCost(System.currentTimeMillis() - readStartTime);
		} catch (IOException e) {
			throw e;
		} finally {
			safeAbort(needAbort, requestBase);
			CloseUtils.close(instream);
		}
	}

	public static byte[] decode(Response res, final HttpEntity entity) throws IOException, FetchException {
		if (entity != null && entity.getContentLength() != 0) {
			final Header ceheader = entity.getContentEncoding();
			if (ceheader != null) {
				final HeaderElement[] codecs = ceheader.getElements();
				for (final HeaderElement codec : codecs) {
					final String codecname = codec.getName().toLowerCase(Locale.US);
					if ("gzip".equals(codecname) || "x-gzip".equals(codecname)) {
						res.setContentEncoding("gzip");
						ExpandedResult result = EncodingUtils.processGzipEncoded(res.getBinary(), actualMaxContentSize);
						if (result.isTruncated()) {
							throw new AbortedFetchException("Truncated", AbortedFetchReason.CONTENT_SIZE);
						}
						return result.getExpanded();
					} else if ("deflate".equals(codecname)) {
						res.setContentEncoding("deflate");
						return EncodingUtils.processDeflateEncoded(res.getBinary());
					} else if ("identity".equals(codecname)) {

						/* Don't need to transform the content - no-op */
						return res.getBinary();
					} else {
						throw new FetchException("Unsupported Content-Coding: " + codec.getName(), FetchException.CODING);
					}
				}
			}
		}
		return res.getBinary();
	}

	public static String getContentType(final HttpEntity entity) throws ParseException {
		Args.notNull(entity, "Entity");
		if (entity.getContentType() != null) {
			return entity.getContentType().getValue();
		}
		return null;
	}

	private static void safeAbort(boolean needAbort, HttpRequestBase request) {
		if (needAbort && (request != null)) {
			try {
				request.abort();
			} catch (Throwable t) {
				// Ignore any errors
			}
		}
	}

	public static Response toResponse(String url, final HttpURLConnection con) throws IOException, FetchException {
		con.addRequestProperty("Accept-Encoding", "gzip");
		con.connect();
		Response res = new Response();
		res.setFetchTime(new Date());
		res.setUrl(url);
		res.setContentType(con.getContentType());
		read0(con.getInputStream(), con.getContentLengthLong(), res);
		byte[] bytes = decode(res, con.getContentEncoding());
		res.setBinary(bytes);
		return res;
	}

	public static byte[] decode(Response res, final String contentEncoding) throws IOException, FetchException {
		if (CommonUtils.isEmpty(contentEncoding)) {
			return res.getBinary();
		}
		final String codecname = contentEncoding.toLowerCase(Locale.US);
		if ("gzip".equals(codecname) || "x-gzip".equals(codecname)) {
			res.setContentEncoding("gzip");
			ExpandedResult result = EncodingUtils.processGzipEncoded(res.getBinary(), actualMaxContentSize);
			if (result.isTruncated()) {
				throw new AbortedFetchException("Truncated", AbortedFetchReason.CONTENT_SIZE);
			}
			return result.getExpanded();
		} else if ("deflate".equals(codecname)) {
			res.setContentEncoding("deflate");
			return EncodingUtils.processDeflateEncoded(res.getBinary());
		} else if ("identity".equals(codecname)) {
			/* Don't need to transform the content - no-op */
			return res.getBinary();
		} else {
			throw new FetchException("Unsupported Content-Coding: " + codecname, FetchException.CODING);
		}
	}

	private static void read0(final InputStream instream, final long contentLength, Response res) throws IOException, FetchException {
		if (instream == null) {
			return;
		}
		try {
			int i = (int) contentLength;
			if (i < 0) {
				i = 4096;
			}
			final ByteArrayBuffer buffer = new ByteArrayBuffer(i);
			final byte[] tmp = new byte[8192];
			int bytesRead = 0;
			int totalRead = 0;
			long readStartTime = res.getFetchTime().getTime();
			long readRate = 0;
			int readRequests = 0;// 读取的次数

			while ((bytesRead = instream.read(tmp)) != -1) {
				readRequests += 1;
				buffer.append(tmp, 0, bytesRead);
				totalRead += bytesRead;

				// Assume read time is at least one millisecond, to avoid DBZ
				// exception.
				long totalReadTime = Math.max(1, System.currentTimeMillis() - readStartTime);
				readRate = (totalRead * 1000L) / totalReadTime;

				// Don't bail on the first read cycle, as we can get a hiccup
				// starting out.
				// Also don't bail if we've read everything we need.
				if ((readRequests > 1) && (totalRead < maxContentSize) && (readRate < minResponseRate)) {
					throw new AbortedFetchException("Slow response rate of " + readRate + " KB/s", AbortedFetchReason.SLOW_RESPONSE_RATE);
				}
				// Check to see if we got interrupted, but don't clear the
				// interrupted flag.
				if (Thread.currentThread().isInterrupted()) {
					throw new AbortedFetchException(AbortedFetchReason.INTERRUPTED);
				}
			}
			res.setBinary(buffer.toByteArray());
			res.setRate((int) (readRate / 1024));
			res.setCost(System.currentTimeMillis() - readStartTime);
		} catch (IOException e) {
			throw e;
		} finally {
			CloseUtils.close(instream);
		}
	}
}
