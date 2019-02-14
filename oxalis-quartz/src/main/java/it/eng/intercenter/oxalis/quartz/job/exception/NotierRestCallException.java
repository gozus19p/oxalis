package it.eng.intercenter.oxalis.quartz.job.exception;

/**
 * Gestisce le eccezioni che si verificano in sede di gestione delle REST call.
 * 
 * @author Manuel Gozzi
 */
public class NotierRestCallException extends Exception {

	public NotierRestCallException(String message) {
		super(message);
	}

}
