package it.eng.intercenter.oxalis.quartz.ws;

import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_USING_REST_URI;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_WRONG_HTTP_PROTOCOL;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_WRONG_INPUT_OUTPUT;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.GsonBuilder;

import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.quartz.job.exception.NotierRestCallException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestManagement {
	
	private static final String DN_KEY = "X-FwdCertSubject_0";
	private static final String DN_VALUE = "CN=ONOTIER, OU=IntercentER, O=Regione Emilia-Romagna, L=Bologna, ST=BO, C=IT";
	private static final String SN_KEY = "X-FwdCertSerialNumber_0";
	private static final String SN_VALUE = "96";

	/**
	 * Esegue una chiamata REST prendendo in input un URI e restituisce la risposta
	 * in formato stringa.
	 * 
	 * @param restUri
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String executeRestCallFromURI(String restUri, NotierRestCallTypeEnum restCallType,
			OxalisMdn oxalisMdn) throws NotierRestCallException {
		try {
			HttpClient client = HttpClients.createDefault();
			log.info(MESSAGE_USING_REST_URI, restUri);

			if (restCallType.equals(NotierRestCallTypeEnum.GET)) {
				HttpGet request = new HttpGet(restUri);
				addCertHeaders(request);
				
				HttpResponse response = client.execute(request);

				if (response.getStatusLine().getStatusCode() != 200) {
					throw new NotierRestCallException("HTTP " + response.getStatusLine().getStatusCode());
				} else {
					log.info(MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE, response.getStatusLine().getStatusCode());
				}

				return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());

			} else if (restCallType.equals(NotierRestCallTypeEnum.POST)) {
				HttpPost request = new HttpPost(restUri);
				addCertHeaders(request);

				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("oxalisMdnJson",
						new GsonBuilder().setPrettyPrinting().create().toJson(oxalisMdn)));
				request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
				HttpResponse response = client.execute(request);

				log.info(MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE, response.getStatusLine().getStatusCode());
				return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());

			} else {
				throw new NotierRestCallException("Rest call type undefined (" + restCallType.toString() + ")!");
			}
		} catch (ClientProtocolException e) {
			log.error(MESSAGE_WRONG_HTTP_PROTOCOL, e.getMessage());
			throw new NotierRestCallException(e.getMessage());
		} catch (IOException e) {
			log.error(MESSAGE_WRONG_INPUT_OUTPUT, e.getMessage());
			throw new NotierRestCallException(e.getMessage());
		}
	}
	
	private static void addCertHeaders(HttpGet request) {
		request.setHeader(SN_KEY, SN_VALUE);
		request.setHeader(DN_KEY, DN_VALUE);
	}
	
	private static void addCertHeaders(HttpPost request) {
		request.setHeader(SN_KEY, SN_VALUE);
		request.setHeader(DN_KEY, DN_VALUE);
	}

}
