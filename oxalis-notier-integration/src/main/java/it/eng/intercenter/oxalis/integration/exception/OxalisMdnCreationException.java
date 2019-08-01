package it.eng.intercenter.oxalis.integration.exception;

/**
 * Eccezione che viene lanciata in sede di tentativo di creazione notifica MDN.
 * Flusso: outbound.
 *
 * @author Manuel Gozzi
 */
public class OxalisMdnCreationException extends Exception {

	private static final long serialVersionUID = 1L;

	public OxalisMdnCreationException(String message) {
		super(message);
	}

	public OxalisMdnCreationException() {
		super();
	}

	public OxalisMdnCreationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public OxalisMdnCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public OxalisMdnCreationException(Throwable cause) {
		super(cause);
	}

}
