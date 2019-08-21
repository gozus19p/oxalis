package it.eng.intercenter.oxalis.quartz.job;

import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_OUTBOUND_FAILED_FOR_URN;
import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_OUTBOUND_SUCCESS_FOR_URN;
import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_STARTING_TO_PROCESS_URN;

import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.StringUtils;

import com.google.inject.Inject;

import it.eng.intercenter.oxalis.integration.dto.NotierDocumentIndex;
import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.UrnList;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisStatusEnum;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import it.eng.intercenter.oxalis.quartz.job.service.OutboundService;
import it.eng.intercenter.oxalis.rest.client.http.HttpCaller;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;

/**
 * Job che si occupa dell'acquisizione e dell'invio dei documenti da Notier
 * verso rete Peppol.
 *
 * @author Manuel Gozzi
 */
@Slf4j
public class OutboundJob implements Job {

	@Inject
	OutboundService outboundService;

	/**
	 * Esegue una chiamata a Notier per recuperare i documenti dal WS relativo.
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		/**
		 * Phase 0: setup REST configuration if needed.
		 */
		outboundService.setupOutboundRestConfiguration();

		/**
		 * Phase 1: get URN of documents that need to be sent on Peppol directly from
		 * Notier via REST web service.
		 */
		String jsonUrnGetterResponse = null;
		try {
			jsonUrnGetterResponse = HttpCaller.executeGet(outboundService.getCertificateConfigManager(), restUrnGetterUri);
		} catch (Exception e) {
			throw new JobExecutionException("Empty response from URI " + restUrnGetterUri);
		} finally {
			if (StringUtils.isEmpty(jsonUrnGetterResponse)) {
				log.error("Received response is empty");
				throw new JobExecutionException("Received response is empty");
			}
		}

		/**
		 * Phase 1b: check the received response and parse it as UrnList object.
		 */
		log.info("Received reponse: {}{}", new Object[] { System.getProperty("line.separator"), jsonUrnGetterResponse });
		UrnList urnListRetrievedFromNotier = GsonUtil.getInstance().fromJson(jsonUrnGetterResponse, UrnList.class);

		if (urnListRetrievedFromNotier != null) {
			log.info("Found {} documents to send on Peppol", urnListRetrievedFromNotier.getUrnCount());
		} else {
			log.error("Invalid response received from Notier: {}{}", new Object[] { System.getProperty("line.separator"), urnListRetrievedFromNotier });
			throw new JobExecutionException("Invalid response received from Notier (UrnList)");
		}

		/**
		 * Phase 2: iterate over UrnList.NotierDocumentIndex' collection in order to
		 * send each document one by one.
		 */
		OxalisMdn oxalisMdn = null;

		for (NotierDocumentIndex index : urnListRetrievedFromNotier.getDocuments()) {
			log.info(MESSAGE_STARTING_TO_PROCESS_URN, index.getUrn());

			try {
				/**
				 * Phase 2a: get document payload by REST web service from Notier.
				 */
				String peppolMessageJson = HttpCaller.executeGet(outboundService.getCertificateConfigManager(), restDocumentGetterUri + index.getUrn());
				log.info("Received String json response containing {} characters", peppolMessageJson.length());
				/**
				 * Phase 2b: build TransmissionMessage object and send that on Peppol network.
				 * The status of the transaction determines how the Oxalis Mdn needs to be
				 * created.
				 */
				oxalisMdn = outboundService.buildTransmissionAndSendOnPeppol(index.getUrn(), peppolMessageJson);
				log.info(MESSAGE_OUTBOUND_SUCCESS_FOR_URN, index.getUrn());
			} catch (UnsupportedOperationException | IOException e) {
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
			} catch (OxalisTransmissionException e) {
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
			} catch (OxalisContentException e) {
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
			} catch (Exception e) {
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
			}

			/**
			 * Phase 3: forward the OxalisMdn object to Notier in order to communicate the
			 * status of transaction.
			 */
			outboundService.sendStatusToNotier(oxalisMdn, index.getUrn());
		}

	}

}
