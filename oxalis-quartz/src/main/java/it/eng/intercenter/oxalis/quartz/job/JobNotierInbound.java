package it.eng.intercenter.oxalis.quartz.job;

import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_READING_PROPERTY;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.StringUtils;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import it.eng.intercenter.oxalis.integration.dto.ReceivedDocument;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.integration.exception.RestConfigException;
import it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCall;
import it.eng.intercenter.oxalis.quartz.job.exception.NotierRestCallException;
import it.eng.intercenter.oxalis.quartz.ws.RestManagement;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class JobNotierInbound implements Job {

	private Path inboundPath;

	private static final String REST_DOCUMENT_INBOUND_CONFIG_KEY = "rest.notier.inbound";

	/**
	 * Variable useful to process REST calls.
	 */
	private static String notierDocumentInbound;

	@Inject
	ConfigRestCall configRestCall;

	/**
	 * @param inboundPath the path where Oxalis persists documents received in
	 *                    inbound flow, managed by Guice
	 */
	@Inject
	public JobNotierInbound(@Named("inbound") Path inboundPath) {
		log.info("The inbound Job {} will search for received documents in path {}",
				new Object[] { JobNotierInbound.class.getName(), inboundPath.normalize().toString() });
		this.inboundPath = inboundPath;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try{
			checkRestConfiguration();
		} catch(RestConfigException e) {
			log.error("Configuration problems found with root cause: {}", e.getMessage());
			log.error("{}", e);
			return;
		}

		log.info("Starting to send received documents to Notier");
		File inboundDir = new File(inboundPath.normalize().toString());
		log.debug("Filtering only files, excluding directories");
		List<File> files = Arrays.asList(inboundDir.listFiles()).stream().filter(file -> file.isFile())
				.collect(Collectors.toList());
		log.info("Total number of files to send to Notier: {}", files.size());
		for (File file : files) {
			log.info("Sending file {}", file.getName());
			try {
				ReceivedDocument document = new ReceivedDocument(file.getName(), new Date(),
						new ByteArrayInputStream(getPayloadFromFile(file)));
				String response = RestManagement.executeRestCallFromURI(notierDocumentInbound,
						NotierRestCallTypeEnum.POST, document);
				log.info("Received response: {}", response);
			} catch (IOException e) {
				log.error("Problems occur while parsing {} file content, root cause: {}",
						new Object[] { file.getName(), e.getMessage() });
				log.error("{}", e);
			} catch (NotierRestCallException e) {
				log.error("Some problems occur while executing REST call with root cause: {}", e.getMessage());
				log.error("{}", e);
			}
		}
	}

	private void checkRestConfiguration() throws RestConfigException {
		if (StringUtils.isEmpty(notierDocumentInbound)) {
			log.info(MESSAGE_READING_PROPERTY, REST_DOCUMENT_INBOUND_CONFIG_KEY);
			notierDocumentInbound = configRestCall.readSingleProperty(REST_DOCUMENT_INBOUND_CONFIG_KEY);
			if (StringUtils.isEmpty(notierDocumentInbound)) {
				throw new RestConfigException("Inbound REST URI has not been properly set up");
			}
		}
	}

	/**
	 * Get the file content out from a File instance.
	 * 
	 * @param file is the file whose content needs to be extracted
	 * @return the content of the file in byte array format
	 * @throws IOException if the file is inaccessible or some other kind of
	 *                     problems occur while accessing the file
	 */
	private final static byte[] getPayloadFromFile(File file) throws IOException {
		return Files.toByteArray(file);
	}

}
