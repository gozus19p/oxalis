package it.eng.intercenter.oxalis.rest.http.impl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import it.eng.intercenter.oxalis.config.impl.CertificateConfigManager;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.rest.http.HttpNotierCall;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpNotierPost extends HttpNotierCall<HttpPost> {

	public HttpNotierPost(CertificateConfigManager certConfig, String uri, BasicNameValuePair... params) throws UnsupportedEncodingException {
		super(certConfig);
		log.info("Creating new POST request with URI {} and {} params", uri, params.length);
		request = new HttpPost(uri);
		request.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), StandardCharsets.UTF_8.toString()));
		requestType = NotierRestCallTypeEnum.POST;
	}

}
