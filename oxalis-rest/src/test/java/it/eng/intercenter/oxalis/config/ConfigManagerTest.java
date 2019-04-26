package it.eng.intercenter.oxalis.config;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.inject.Inject;

import it.eng.intercenter.oxalis.config.impl.CertificateConfigManager;
import it.eng.intercenter.oxalis.config.impl.EmailSenderConfigManager;
import it.eng.intercenter.oxalis.config.impl.RestConfigManager;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;

/**
 * @author Manuel Gozzi
 */
@Slf4j
@Guice(modules = GuiceModuleLoader.class)
public class ConfigManagerTest {
	
	private static final String EMPTY = "";

	@Inject
	CertificateConfigManager certificateConfigManager;
	
	@Inject
	EmailSenderConfigManager emailSenderConfigManager;
	
	@Inject
	RestConfigManager restConfigManager;
	
	@Test
	public void testCertificateConfig() {
		log.info("Starting to test: {}", CertificateConfigManager.class.getName());
		
		Properties prop = certificateConfigManager.getFullConfiguration();
		Assert.assertNotNull(prop);
		
		/**
		 * Assert that exist a value for each defined key field.
		 */
		List<Field> keyFields = certificateConfigManager.getConfigurationKeyFields(CertificateConfigManager.class);
		keyFields.stream().forEach(field -> {
			try {
				String value = certificateConfigManager.readValue((String) field.get(CertificateConfigManager.class));
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
		
		Properties prop = restConfigManager.getFullConfiguration();
		Assert.assertNotNull(prop);
		
		/**
		 * Assert that exist a value for each defined key field.
		 */
		List<Field> keyFields = restConfigManager.getConfigurationKeyFields(RestConfigManager.class);
		keyFields.stream().forEach(field -> {
			try {
				String value = restConfigManager.readValue((String) field.get(RestConfigManager.class));
				Assert.assertNotNull(value);
				Assert.assertNotEquals(value.trim(), EMPTY);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Assert.fail();
			}
		});
	}
	
}
