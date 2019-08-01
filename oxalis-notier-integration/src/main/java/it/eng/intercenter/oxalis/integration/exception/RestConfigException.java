package it.eng.intercenter.oxalis.integration.exception;

/**
 * @author Manuel Gozzi
 */
public class RestConfigException extends Exception {

	private static final long serialVersionUID = 1L;

	public RestConfigException(String message) {
		super(message);
	}

	public RestConfigException() {
		super();
	}

	public RestConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RestConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public RestConfigException(Throwable cause) {
		super(cause);
	}

}
