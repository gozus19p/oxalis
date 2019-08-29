package it.eng.intercenter.oxalis.integration.dto;

import java.util.Date;

/**
 * @author Manuel Gozzi
 * @date 29 ago 2019
 * @time 16:13:31
 */
public class OxalisLookupEndpoint {

	private String address;
	private Date serviceActivationDate;
	private Date serviceExpirationDate;
	private String certificate;
	private String serviceDescription;
	private String technicalContactUrl;
	private String technicalInformationUrl;
	private String transportProfile;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Date getServiceActivationDate() {
		return serviceActivationDate;
	}

	public void setServiceActivationDate(Date serviceActivationDate) {
		this.serviceActivationDate = serviceActivationDate;
	}

	public Date getServiceExpirationDate() {
		return serviceExpirationDate;
	}

	public void setServiceExpirationDate(Date serviceExpirationDate) {
		this.serviceExpirationDate = serviceExpirationDate;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public String getTechnicalContactUrl() {
		return technicalContactUrl;
	}

	public void setTechnicalContactUrl(String technicalContactUrl) {
		this.technicalContactUrl = technicalContactUrl;
	}

	public String getTechnicalInformationUrl() {
		return technicalInformationUrl;
	}

	public void setTechnicalInformationUrl(String technicalInformationUrl) {
		this.technicalInformationUrl = technicalInformationUrl;
	}

	public String getTransportProfile() {
		return transportProfile;
	}

	public void setTransportProfile(String transportProfile) {
		this.transportProfile = transportProfile;
	}

}
