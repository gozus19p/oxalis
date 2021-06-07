package it.eng.intercenter.oxalis.onboarding.rest.server.servlet;

import com.google.inject.Inject;
import it.eng.intercenter.oxalis.onboarding.core.IOxalisOnboardingService;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.lang.OxalisTransmissionException;
import org.apache.commons.io.IOUtils;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class OutboundFlowServlet extends HttpServlet {

	private static final String ALLOWED_DN = "";
	private final IOxalisOnboardingService oxalisOnboardingService;

	@Inject
	public OutboundFlowServlet(IOxalisOnboardingService oxalisOnboardingService) {
		this.oxalisOnboardingService = oxalisOnboardingService;
	}

	@Override
	public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {

		try {

			// Check if client used client certificate
			checkClientCertificate(httpServletRequest);

			// Parsing input as String message
			String message = IOUtils.toString(httpServletRequest.getInputStream(), StandardCharsets.UTF_8.name());

			// Sending message using outbound component
			byte[] receipt = oxalisOnboardingService.sendOutbound(message);

			// Check for receipt status
			if (receipt != null && receipt.length > 0) {

				httpServletResponse.setStatus(200);
				httpServletResponse.getWriter().write(
						new String(receipt)
				);
			} else {

				throw new OxalisTransmissionException("No response received");
			}

		} catch (SecurityException e) {

			log.warn("Unhautorized: {}", e.getMessage());
			httpServletResponse.setStatus(403);
		} catch (Exception e) {

			log.error("An error occurred: {}", e.getMessage(), e);
			httpServletResponse.setStatus(500);
			httpServletResponse.getWriter().write(getMessage(e));
		}
	}

	/**
	 * It checks for client certificate status.
	 *
	 * @param httpServletRequest is the HTTP request received by Oxalis server
	 * @throws SecurityException if something goes wrong during security check
	 */
	private void checkClientCertificate(HttpServletRequest httpServletRequest) throws SecurityException {

		X509Certificate[] clientCertificates = (X509Certificate[]) httpServletRequest.getAttribute("javax.servlet.request.X509Certificate");
		if (clientCertificates == null || clientCertificates.length == 0) {

			throw new SecurityException("No client certificate provided");
		}

		// Retrieving client certificate
		X509Certificate clientCert = clientCertificates[0];
		X500Principal subjectDN = clientCert.getSubjectX500Principal();
		String name = subjectDN.getName();

		if (!ALLOWED_DN.equals(name)) {

			throw new SecurityException(
					String.format(
							"Distinguish name [%s] is not allowed",
							name
					)
			);
		}
	}

	/**
	 * Get complete message for error including other causes.
	 *
	 * @param e is the {@link Throwable} instance thrown
	 * @return the whole String message
	 */
	private String getMessage(Throwable e) {

		return e.getCause() != null ?
				getMessage(e.getCause())
				: e.getMessage() + "; ";
	}
}
