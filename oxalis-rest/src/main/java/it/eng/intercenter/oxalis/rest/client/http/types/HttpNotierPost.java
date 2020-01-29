package it.eng.intercenter.oxalis.rest.client.http.types;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.rest.client.api.AbstractHttpNotierCall;
import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class HttpNotierPost extends AbstractHttpNotierCall<HttpPost> {

	public HttpNotierPost(CertificateConfigManager certConfig, String uri, BasicNameValuePair... params) throws UnsupportedEncodingException {
		super(certConfig);
		log.debug("Creating new POST request with URI {} and {} params", uri, params.length);
		httpRequest = new HttpPost(uri);
		httpRequest.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), StandardCharsets.UTF_8.toString()));
		httpRequestType = NotierRestCallTypeEnum.POST;
	}

}
