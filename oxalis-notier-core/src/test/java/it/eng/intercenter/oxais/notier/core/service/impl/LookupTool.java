package it.eng.intercenter.oxais.notier.core.service.impl;

import network.oxalis.vefa.peppol.common.lang.PeppolLoadingException;
import network.oxalis.vefa.peppol.common.model.*;
import network.oxalis.vefa.peppol.lookup.LookupClient;
import network.oxalis.vefa.peppol.lookup.LookupClientBuilder;
import network.oxalis.vefa.peppol.lookup.fetcher.UrlFetcher;
import network.oxalis.vefa.peppol.lookup.locator.BdxlLocator;
import network.oxalis.vefa.peppol.lookup.provider.DefaultProvider;
import network.oxalis.vefa.peppol.mode.Mode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manuel Gozzi
 */
public class LookupTool {

	private static final String[] PARTICIPANTS = {
			// TODO insert participant to lookup for
	};

	public static void main(String[] args) throws PeppolLoadingException {

		String modeString = "PRODUCTION";

		LookupClientBuilder builder = LookupClientBuilder.newInstance(Mode.of(modeString));
		builder.locator(new BdxlLocator(Mode.of(modeString)));
		builder.fetcher(new UrlFetcher(Mode.of(modeString)));
		builder.provider(new DefaultProvider());
		LookupClient client = builder.build();

		Arrays.stream(PARTICIPANTS).forEach(p -> {
			System.out.print(
					"Participant Identifier: " + p + " [ "
			);

			ServiceMetadata references;
			try {
				references = client.getServiceMetadata(
						ParticipantIdentifier.of(p),
						DocumentTypeIdentifier.of("urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:fdc:peppol.eu:poacc:trns:order:3:restrictive:urn:www.agid.gov.it:trns:ordine:3.1::2.1")
				);
				List<List<Endpoint>> endpoints = references.getProcesses()
						.stream()
						.map(ProcessMetadata::getEndpoints)
						.collect(Collectors.toList());
				for (List<Endpoint> endpoint : endpoints) {
					endpoint.stream()
							.map(Endpoint::getAddress)
							.collect(Collectors.toList())
							.forEach(el -> System.out.print(el + "; "));
				}
			} catch (Exception e) {
				System.out.print(e.getMessage());
			}
			System.out.println("]");
		});

	}
}
