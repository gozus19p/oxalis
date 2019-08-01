package it.eng.intercenter.oxalis.integration.exception;

/**
 * Eccezione che viene restituita nel caso in cui i metadati non siano
 * congruenti alle informazioni presenti sul documento. Flusso: inbound.
 *
 * @author Manuel Gozzi
 */
public class InconsistentPeppolMetadataException extends Exception {

	private static final long serialVersionUID = 1L;

	public InconsistentPeppolMetadataException(String message) {
		super(message);
	}

	public InconsistentPeppolMetadataException() {
		super();
	}

	public InconsistentPeppolMetadataException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InconsistentPeppolMetadataException(String message, Throwable cause) {
		super(message, cause);
	}

	public InconsistentPeppolMetadataException(Throwable cause) {
		super(cause);
	}

}
