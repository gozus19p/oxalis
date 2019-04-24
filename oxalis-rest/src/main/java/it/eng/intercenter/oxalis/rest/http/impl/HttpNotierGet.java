package it.eng.intercenter.oxalis.rest.http.impl;

import org.apache.http.client.methods.HttpGet;

import it.eng.intercenter.oxalis.config.impl.ConfigNotierCertificate;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.rest.http.HttpNotierCall;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpNotierGet extends HttpNotierCall<HttpGet> {

	public HttpNotierGet(ConfigNotierCertificate certConfig, String uri) {
		super(certConfig);
		log.info("Creating new GET request with URI {}", uri);
		request = new HttpGet(uri);
		log.info("Request created");
		requestType = NotierRestCallTypeEnum.GET;
	}

}
