package it.eng.intercenter.oxalis.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.EmailSenderConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.RestConfigManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class ConfigManagerTest {
	
	private static final String EMPTY = "";

	CertificateConfigManager certificateConfigManager;
	
	EmailSenderConfigManager emailSenderConfigManager;
	
	RestConfigManager restConfigManager;
	
	@Test
	public void testCertificateConfig() {
		log.info("Starting to test: {}", CertificateConfigManager.class.getName());
		try {
			certificateConfigManager = new CertificateConfigManager(Paths.get("C:\\Users\\MGozzi\\.oxalis"));
		} catch (IOException e1) {
			Assert.fail();
		}
		Properties prop = certificateConfigManager.getFullConfiguration();
		Assert.assertNotNull(prop);
		
		/**
		 * Assert that exist a value for each defined key field.
		 */
		List<Field> keyFields = certificateConfigManager.getConfigurationKeyFields();
		keyFields.stream().forEach(field -> {
			try {
				String value = certificateConfigManager.readValue((String) field.get(certificateConfigManager.getClass()));
				Assert.assertNotNull(value);
				Assert.assertNotEquals(value.trim(), EMPTY);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Assert.fail();
			}
		});
	}
	
	@Test
	public void testRestConfig() {
		log.info("Starting to test: {}", RestConfigManager.class.getName());
		
		try {
			restConfigManager = new RestConfigManager(Paths.get("C:\\Users\\MGozzi\\.oxalis"));
		} catch (IOException e1) {
			Assert.fail();
		}
		
		Properties prop = restConfigManager.getFullConfiguration();
		Assert.assertNotNull(prop);
		
		/**
		 * Assert that exist a value for each defined key field.
		 */
		List<Field> keyFields = restConfigManager.getConfigurationKeyFields();
		keyFields.stream().forEach(field -> {
			try {
				String value = restConfigManager.readValue((String) field.get(restConfigManager.getClass()));
				Assert.assertNotNull(value);
				Assert.assertNotEquals(value.trim(), EMPTY);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Assert.fail();
			}
		});
	}
	
}
