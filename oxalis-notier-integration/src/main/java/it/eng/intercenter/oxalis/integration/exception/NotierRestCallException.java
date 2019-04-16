package it.eng.intercenter.oxalis.integration.exception;

/**
 * Gestisce le eccezioni che si verificano in sede di gestione delle REST call.
 * 
 * @author Manuel Gozzi
 */
public class NotierRestCallException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotierRestCallException(String message) {
		super(message);
	}

}
