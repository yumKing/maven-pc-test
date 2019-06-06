package org.jin.httpclient.utils;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class CloseUtils {

	public static void close(AutoCloseable close) {
		if (close != null) {
			try {
				close.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void close(Connection close) {
		if (close != null) {
			try {
				close.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void close(ResultSet close) {
		if (close != null) {
			try {
				close.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public static void close(Statement close) {
		if (close != null) {
			try {
				close.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static void clean(final Object buffer) {
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				try {
					Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
					getCleanerMethod.setAccessible(true);
					sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
					cleaner.clean();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});

	}
}
