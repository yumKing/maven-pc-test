package org.jin.httpclient;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;


import org.jin.httpclient.cookieFactory.CookieFactory;
import org.jin.httpclient.cookieFactory.CookieInfo;
import org.jin.httpclient.cookieFactory.CookieServiceConstants;
import org.jin.httpclient.enums.UserAgent;
import org.jin.httpclient.exception.AbortedFetchException;
import org.jin.httpclient.exception.FetchException;
import org.jin.httpclient.proxy.ProxyServer;
import org.jin.httpclient.proxy.ProxyServerDao;
import org.jin.httpclient.utils.CloseUtils;
import org.jin.httpclient.utils.CommonUtils;
import org.jin.httpclient.utils.LoggerUtils;

@ThreadSafe
public abstract class AbstractHttpClient extends AbstractRequest{
	
	//默认超时时间设置
	private static int defaultTimeout = 2500;
	//默认的http连接工具
	private volatile CloseableHttpClient client;
	//cookie store
	private CookieStore cookieStore;
	//http构建配置信息
	private static RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setSocketTimeout(defaultTimeout)
            .setConnectTimeout(defaultTimeout)
            .setConnectionRequestTimeout(defaultTimeout)
            .build();
	//socket构建配置
	private static SocketConfig socketConfig = SocketConfig.custom()
            .setTcpNoDelay(true)
            .setSoTimeout(defaultTimeout)
            .build();
	//http连接池管理
	private static PoolingHttpClientConnectionManager connManager=null;
	
	//监控线程
	private static IdleConnectionMonitorThread monitor = null;
	
	private ConnectionKeepAliveStrategy myStrategy = new MyConnectionKeepAliveStrategy();
    static {
        if(connManager == null){
			initConnectManager();
        }
    }
    private boolean initFlag = false;
    
    @SuppressWarnings("deprecation")
	private static void  initConnectManager(){
		SSLContext sslContext = null;
		try {
			sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
			SSLConnectionSocketFactory sslCSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			connManager = new PoolingHttpClientConnectionManager(
					RegistryBuilder.<ConnectionSocketFactory>create()
							.register("http", new PlainConnectionSocketFactory(){
								@Override
							    public Socket createSocket(final HttpContext context) throws IOException {
									Socket socket= new Socket();
									socket.setSoTimeout(30000);
									return socket;
								}
							})
							.register("https", sslCSF)
							.build(),null,null,null,300,TimeUnit.SECONDS);
			connManager.setDefaultSocketConfig(socketConfig);
			connManager.setMaxTotal(1000);
			/*ThreadPool.getSchedulePool().scheduleAtFixedRate(
                    monitor=new IdleConnectionMonitorThread(connManager),
					0,
					15,
					TimeUnit.SECONDS);*/
		} catch (NoSuchAlgorithmException e) {
		} catch (KeyManagementException e) {
		} catch (KeyStoreException e) {
		}
	}
	
	public AbstractHttpClient() {
		this(null);
	}

	public AbstractHttpClient(CookieFactory cookieFactory) {
		super(cookieFactory);
		initClient();
	}
	
	private void initClient() {
        if(initFlag){
           return;
        }
        initFlag = true;
        if(connManager == null){
            initConnectManager();
        }
        try {
			cookieStore = new BasicCookieStore();
			HttpClientBuilder builder = HttpClientBuilder.create().disableContentCompression()
					.setConnectionManager(connManager)
					.setConnectionReuseStrategy(new DefaultConnectionReuseStrategy()).setUserAgent(UserAgent.CHROME670.getName())
                    .setRetryHandler(RetryHandler.Instance);
			builder.addInterceptorLast(new RequestAcceptEncoding());
			builder.setDefaultCookieStore(cookieStore);
            builder.setDefaultRequestConfig(defaultRequestConfig);
            builder.setKeepAliveStrategy(myStrategy);
            client = builder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public final List<Cookie> getCookies() {
		return cookieStore.getCookies();
	}

	public final void addCookie(Cookie cookie) {
		cookieStore.addCookie(cookie);
	}

	public final boolean clearExpired(Date date) {
		return cookieStore.clearExpired(date);
	}

	@Override
	public final void clearCookies() {
		cookieStore.clear();
	}

	@Override
	public synchronized void close() throws Exception {
		client.close();
		connManager = null;
		if(monitor != null){
            monitor.shutdown();
        }
		initFlag = false;
	}
	
	
	@Override
	public Response get(String url, Map<String, String> headers, Charset charset) throws FetchException {
		return null;
	}

	@Override
	public Response post(String url, Map<String, String> headers, HttpEntity entity) throws FetchException {
		return null;
	}

	
	static class IdleConnectionMonitorThread extends Thread {
        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(PoolingHttpClientConnectionManager connMgr) {
            super();
            this.setName("http-connection-monitor");
            this.setDaemon(true);
            this.connMgr = connMgr;
        }
        @Override
        public void run() {
            if(!shutdown){
                LoggerUtils.info(AbstractHttpClient.class, "Closing expired connections");
                connMgr.closeExpiredConnections(); // 关闭过期的链接
                connMgr.closeIdleConnections(60, TimeUnit.SECONDS); // 选择关闭 空闲60秒的链接
            }
        }
        public synchronized void shutdown(){
            if(!shutdown){
                shutdown =true;
            }
        }
    }
    private class MyConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy{

        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            // Honor 'keep-alive' header
            HeaderElementIterator it = new BasicHeaderElementIterator(
                    response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch(NumberFormatException ignore) {
                    }
                }
            }
            HttpHost target = (HttpHost) context.getAttribute(
                    HttpClientContext.HTTP_TARGET_HOST);
            if ("www.naughty-servCebneter.com".equalsIgnoreCase(target.getHostName())) {
                // Keep alive for 5 seconds only
                return 5 * 1000;
            } else {
                // otherwise keep alive for 30 seconds
                return 30 * 1000;
            }
        }
    }
	
    public final Response execute(HttpRequestBase request) throws FetchException {
		return execute(request, null);
	}

	public final Response execute(HttpRequestBase request, String url) throws FetchException {
		LoggerUtils.info(this.getClass(),String.format("httpClient send url:%s",url));
        long start = System.currentTimeMillis();
        if (cookieFactory != null) {
			CookieInfo cookie = cookieFactory.getCachedCookie();
			if (cookie != null && cookie != CookieServiceConstants.DEFAULTCOOKIE) {
				request.addHeader("Cookie", cookie.getCookie());
			}
		}
		if (startproxy) {
            Response result = executeWithFlowProxy(request, url);
            LoggerUtils.info(this.getClass(),String.format("http请求耗时：%s ms",(System.currentTimeMillis()-start)));
            return result;
		}
		if (closeproxy) {
            Response result = execute0(request, url);
            LoggerUtils.info(this.getClass(),String.format("http请求耗时：%s ms",(System.currentTimeMillis()-start)));
            return  result;
		}
		HttpHost host = proxy.get();
		if (host != null) {
            Response result = execute0(request, url);
            LoggerUtils.info(this.getClass(),String.format("http请求耗时：%s ms",(System.currentTimeMillis()-start)));
            return result;
		}
		String useProxystr = System.getProperty(Request.USE_SYSTEM_PROXY, "false");
		boolean useProxy = "true".equals(useProxystr);
		Response result = useProxy ? executeWithRandomProxy(request, url) : execute0(request, url);
        LoggerUtils.info(this.getClass(),String.format("http请求耗时：%s ms",(System.currentTimeMillis()-start)));
        return result;
	}

	private Response executeWithFlowProxy(HttpRequestBase request, String url) throws FetchException {
		ProxyServerDao dao = ProxyServerDao.getInstance();
//		int retry = retryCount;
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		TimeOut t = timeout.get();
		if (t == DEFAULT_TIMEOUNT) {
			t = PROXY_TIMEOUNT;
		}
		requestConfigBuilder.setConnectTimeout(t.getConnectRequestTimeout()==0?defaultTimeout:t.getConnectRequestTimeout());
		requestConfigBuilder.setConnectionRequestTimeout(t.getConnectTimeout()==0?defaultTimeout:t.getConnectTimeout());
		requestConfigBuilder.setSocketTimeout(t.getSocketTimeout()==0?defaultTimeout:t.getSocketTimeout());

		Response res = new Response();
		res.setUrl(CommonUtils.isEmpty(url) ? request.getURI().toString() : url);

		FetchException e = null;
		/*
		 * 虽然httpclient默认有个retry，但是在许多情况下，依然不够
		 */
//		while ((retry--) > 0) {
			ProxyServer proxy = null;
			if (flowproxy.get() == null) {
				proxy = dao.randomGet();
				if (proxy != null) {
					requestConfigBuilder.setProxy(proxy.toHttpHost());
					request.setConfig(requestConfigBuilder.build());
					CloseableHttpResponse response = null;
					try {
						response = client.execute(request);
						Response r = Entitys.toResponse(request, response, res);
						flowproxy.set(proxy);
						return r;
					} catch (Exception ee) {
						dao.addFailedProxyServer(proxy);
						e = new FetchException(ee);
					} finally {
						CloseUtils.close(response);
					}
				} else {
					return execute0(request, url);
				}
			} else {
				proxy = flowproxy.get();
				if (proxy != null) {
					requestConfigBuilder.setProxy(proxy.toHttpHost());
					request.setConfig(requestConfigBuilder.build());
					CloseableHttpResponse response = null;
					try {
						response = client.execute(request);
						Response r = Entitys.toResponse(request, response, res);
						return r;
					} catch (Exception ee) {
						e = new FetchException(ee);
					} finally {
						CloseUtils.close(response);
					}
				} else {
					return execute0(request, url);
				}
//			}

		}

		throw e;
	}

	private final Response execute0(HttpRequestBase request, String url) throws FetchException {
//		int retry = retryCount;
		Response res = new Response();
		res.setUrl(CommonUtils.isEmpty(url) ? request.getURI().toString() : url);
//		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
//		Timeout t = timeout.get();
//		requestConfigBuilder.setConnectTimeout(t.connectionRequestTimeout);
//		requestConfigBuilder.setConnectionRequestTimeout(t.connectTimeout);
//		requestConfigBuilder.setSocketTimeout(t.socketTimeout);
        request.setConfig(defaultRequestConfig);
		if (!closeproxy) {
			HttpHost host = proxy.get();
			if (host != null) {
                request.setConfig(RequestConfig.copy(defaultRequestConfig).setProxy(host).build());;
//				requestConfigBuilder.setProxy(host);
			}
		}
		FetchException e;
		/*
		 * 虽然httpclient默认有个retry，但是在许多情况下，依然不够
		 */
//		while ((retry--) > 0) {
			CloseableHttpResponse response = null;
			try {
				response = client.execute(request);
				return Entitys.toResponse(request, response, res);
			} catch (AbortedFetchException ee) {
				e = ee;
//				break;
			} catch (FetchException ee) {
				e = ee;
				if (ee.getErrortype() == FetchException.UNEXPECTED) {// 对于404之类的错误，没必要重复抓取了
//					break;
				}

				if (ee.getErrortype() == FetchException.IMPORTANT) {// 对于重要错误，需要重新设置client
					//没必要。ignore
//					resetClient();
//					break;
				}
			} catch (Exception ee) {
				e = new FetchException(ee);
			} finally {
				CloseUtils.close(response);
			}
//		}
		throw e;
	}

	private final Response executeWithRandomProxy(HttpRequestBase request, String url) throws FetchException {
		ProxyServerDao dao = ProxyServerDao.getInstance();
		int retry = retryCount;
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		TimeOut t = timeout.get();
		if (t == DEFAULT_TIMEOUNT) {
			t = PROXY_TIMEOUNT;
		}
		requestConfigBuilder.setConnectTimeout(t.getConnectRequestTimeout());
		requestConfigBuilder.setConnectionRequestTimeout(t.getConnectTimeout());
		requestConfigBuilder.setSocketTimeout(t.getSocketTimeout());
		Response res = new Response();
		res.setUrl(CommonUtils.isEmpty(url) ? request.getURI().toString() : url);

		FetchException e = null;
		/*
		 * 虽然httpclient默认有个retry，但是在许多情况下，依然不够
		 */
		while ((retry--) > 0) {
			ProxyServer proxy = dao.randomGet();
			if (proxy != null) {
				requestConfigBuilder.setProxy(proxy.toHttpHost());
				request.setConfig(requestConfigBuilder.build());
				CloseableHttpResponse response = null;
				try {
					response = client.execute(request);
					Response r = Entitys.toResponse(request, response, res);
					return r;
				} catch (Exception ee) {
					dao.addFailedProxyServer(proxy);
					e = new FetchException(ee);
				} finally {
					CloseUtils.close(response);
				}
			} else {
				return execute0(request, url);
			}
		}

		throw e;
	}
}
