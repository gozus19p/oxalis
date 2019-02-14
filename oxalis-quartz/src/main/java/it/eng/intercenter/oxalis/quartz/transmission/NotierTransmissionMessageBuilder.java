package it.eng.intercenter.oxalis.quartz.transmission;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import it.eng.intercenter.oxalis.quartz.job.exception.NotierDocumentCastException;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.tag.Tag;
import no.difi.vefa.peppol.common.model.Header;

/**
 * Definisce un elenco di funzionalità atte a costruisce un oggeto
 * TransmissionMessage partendo da un documento ricevuto da Notier.
 * 
 * @author Manuel Gozzi
 */
public class NotierTransmissionMessageBuilder {

	private static final Logger log = LoggerFactory.getLogger(NotierTransmissionMessageBuilder.class);

	/**
	 * Costruisce un TransmissionMessage (NotierTransmissionMessage, creato per
	 * copia da DefaultTransmissionMessage) partendo da un DTO.
	 * 
	 * @param jsonString
	 * @return
	 * @throws NotierDocumentCastException
	 */
	public static TransmissionMessage buildTransmissionMessageFromDocumento(String jsonString)
			throws NotierDocumentCastException {
		Object dto = castFromJsonToObject(jsonString);
		TransmissionMessage message = new NotierTransmissionMessage(extractHeaderFromDocument(dto),
				extractInputStreamFromDocument(dto), extractTagFromDocument(dto));
		return message;
	}

	/**
	 * Traduce una stringa json in oggetto DTO.
	 * 
	 * @param jsonString
	 * @return
	 * @throws NotierDocumentCastException
	 */
	private static Object castFromJsonToObject(String jsonString) throws NotierDocumentCastException {
		Object obj = new Object();
		try {
			obj = new Gson().fromJson(jsonString, Object.class);
			return obj;
		} catch (JsonParseException e) {
			log.error("Something went wrong during cast execution from json to DTO, message: {}", e.getMessage());
			throw new NotierDocumentCastException(e.getMessage());
		}
	}

	/**
	 * Recupera l'Header da un definito DTO che rappresenta il documento.
	 * 
	 * @param obj
	 * @return
	 */
	private static Header extractHeaderFromDocument(Object obj) {
		return new Header();
	}

	/**
	 * Recupera l'InputStream da un definito DTO che rappresenta il documento.
	 * @param obj
	 * @return
	 */
	private static InputStream extractInputStreamFromDocument(Object obj) {
		return null;
	}

	/**
	 * Recupera il Tag da un definito DTO che rappresenta il documento.
	 * @param obj
	 * @return
	 */
	private static Tag extractTagFromDocument(Object obj) {
		return null;
	}

}
