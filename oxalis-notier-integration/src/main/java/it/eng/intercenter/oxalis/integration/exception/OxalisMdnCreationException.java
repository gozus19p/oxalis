package it.eng.intercenter.oxalis.integration.exception;

/**
 * Eccezione che viene lanciata in sede di tentativo di creazione notifica MDN.
 * Flusso: outbound.
 * 
 * @author Manuel Gozzi
 */
public class OxalisMdnCreationException extends Exception {

	/**
	 * @author Manuel Gozzi
	 * @date 25 feb 2019
	 * @time 11:41:05
	 */
	private static final long serialVersionUID = 1L;

	public OxalisMdnCreationException(String message) {
		super(message);
	}

}
