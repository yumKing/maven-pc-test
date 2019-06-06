package org.jin.httpclient.proxy;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;

import org.jin.httpclient.utils.Threads;


public class ProxyServerDao implements Closeable {

	private volatile static ProxyServerDao instance;

	public static ProxyServerDao getInstance() {
		if (instance == null) {
			synchronized (ProxyServerDao.class) {
				if (instance == null) {
					instance = new ProxyServerDao();
				}
			}
		}
		return instance;
	}

	private static Logger logger = Logger.getLogger("ProxyServerDao");

	private Random random = ThreadLocalRandom.current();
	public static final String tableName = "proxy.db";
	private ConcurrentMap<String, ProxyServer> servers = new ConcurrentHashMap<String, ProxyServer>();

	private DB db;

	private ProxyServerDao() {
		Options options = new Options();
		options.createIfMissing(true);

		try {
			db = JniDBFactory.factory.open(new File(tableName), options);
		} catch (IOException e) {
			logger.warning(String.format("%s, %s", tableName, e));
		}
		for (ProxyServer server : getAll()) {
			servers.put(server.toKeyString(), server);
		}
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				ProxyChecker checker = new ProxyChecker();
				while (true) {
					try {
						for (Entry<String, ProxyServer> entry : servers.entrySet()) {
							ProxyServer proxyServer = entry.getValue();
							if (proxyServer.needCheck) {
								if (!checker.check(proxyServer)) {
									servers.remove(proxyServer.toKeyString());
									delete(proxyServer);
								}
								proxyServer.needCheck = false;
							}
						}
						Threads.sleepSecond(1);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.setPriority(Thread.MIN_PRIORITY);
		Threads.setDaemonThreadRunning(thread, "check_failed_proxyservers");
	}

	/**
	 * 有则更新，无则插入
	 * 
	 * @param servers
	 */
	public void put(ProxyServer... servers) {
		put(Arrays.asList(servers));
	}

	/**
	 * 有则更新，无则插入
	 * 
	 * @param servers
	 */

	public void put(List<ProxyServer> servers) {
		WriteBatch wb = db.createWriteBatch();
		for (ProxyServer proxyServer : servers) {
			wb.put(proxyServer.toKey(), proxyServer.toValue());
		}
		db.write(wb);
	}

	public boolean exist(ProxyServer server) {
		return db.get(server.toKey()) != null;
	}

	public void delete(ProxyServer... servers) {
		delete(Arrays.asList(servers));
	}

	public void delete(List<ProxyServer> servers) {
		logger.info("DELETE____" + servers);
		for (ProxyServer server : servers) {
			db.delete(server.toKey());
		}

	}

	/**
	 * 随机获取一个代理
	 * 
	 * @return
	 */
	public ProxyServer randomGet() {
		ProxyServer[] ps = servers.values().toArray(new ProxyServer[0]);
		return ps[random.nextInt(ps.length)];
	}

	public List<ProxyServer> getAll() {
		DBIterator iter = db.iterator();
		iter.seekToFirst();
		List<ProxyServer> res = new ArrayList<ProxyServer>();
		while (iter.hasNext()) {
			Entry<byte[], byte[]> entry = iter.next();
			res.add(ProxyServer.fromValue(entry.getValue()));
		}
		return res;
	}

	public void addFailedProxyServer(ProxyServer server) {
		server.needCheck = true;
	}

	public static void main(String[] args) {
		ProxyServerDao dao = new ProxyServerDao();
		// List<ProxyServer> servers = new ArrayList<>();
		// servers.add(new ProxyServer("192.168.1.1", 80, "HTTP", "中国"));
		// servers.add(new ProxyServer("192.168.1.2", 809, "HTTP", "中国"));
		// dao.put(servers);
		System.out.println(dao.randomGet());
		System.out.println(dao.randomGet());
		System.out.println(dao.randomGet());
		// dao.delete(servers);
		System.out.println(dao.randomGet());
	}

	@Override
	public void close() throws IOException {
		db.close();
	}

}
