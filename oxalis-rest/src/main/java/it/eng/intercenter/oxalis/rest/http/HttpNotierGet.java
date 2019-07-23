package it.eng.intercenter.oxalis.rest.http;

import org.apache.http.client.methods.HttpGet;

import it.eng.intercenter.oxalis.api.AbstractHttpNotierCall;
import it.eng.intercenter.oxalis.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpNotierGet extends AbstractHttpNotierCall<HttpGet> {

	public HttpNotierGet(CertificateConfigManager certConfig, String uri) {
		super(certConfig);
		log.info("Creating new GET request with URI {}", uri);
		httpRequest = new HttpGet(uri);
		httpRequestType = NotierRestCallTypeEnum.GET;
	}

}
