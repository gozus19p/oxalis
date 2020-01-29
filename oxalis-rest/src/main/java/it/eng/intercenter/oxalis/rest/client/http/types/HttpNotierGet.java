package it.eng.intercenter.oxalis.rest.client.http.types;

import org.apache.http.client.methods.HttpGet;

import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.rest.client.api.AbstractHttpNotierCall;
import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class HttpNotierGet extends AbstractHttpNotierCall<HttpGet> {

	public HttpNotierGet(CertificateConfigManager certConfig, String uri) {
		super(certConfig);
		log.debug("Creating new GET request with URI {}", uri);
		httpRequest = new HttpGet(uri);
		httpRequestType = NotierRestCallTypeEnum.GET;
	}

}
