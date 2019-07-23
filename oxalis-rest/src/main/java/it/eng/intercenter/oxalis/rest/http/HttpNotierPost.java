package it.eng.intercenter.oxalis.rest.http;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import it.eng.intercenter.oxalis.api.AbstractHttpNotierCall;
import it.eng.intercenter.oxalis.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpNotierPost extends AbstractHttpNotierCall<HttpPost> {

	public HttpNotierPost(CertificateConfigManager certConfig, String uri, BasicNameValuePair... params) throws UnsupportedEncodingException {
		super(certConfig);
		log.info("Creating new POST request with URI {} and {} params", uri, params.length);
		httpRequest = new HttpPost(uri);
		httpRequest.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), StandardCharsets.UTF_8.toString()));
		httpRequestType = NotierRestCallTypeEnum.POST;
	}

}
