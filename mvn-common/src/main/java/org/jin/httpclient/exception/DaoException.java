package org.jin.httpclient.exception;


public class DaoException extends Exception {

	private static final long serialVersionUID = 1L;

	public DaoException() {
		super();
	}

	public DaoException(String msg) {
		super(msg);
	}

	public DaoException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public DaoException(Throwable cause) {
		super(cause);
	}

}
