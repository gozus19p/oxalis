package it.eng.intercenter.oxalis.quartz.transmission.notify;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.inject.Inject;

import it.eng.intercenter.oxalis.quartz.config.ConfigEmailNotificationSender;

/**
 * 
 * @author Manuel Gozzi
 */
public class EmailNotificationSender {
	
	@Inject
	ConfigEmailNotificationSender config;
	
	//TODO: Creare template HTML
	//TODO: Gestire configurazione delle properties via e-mail.

	/**
	 * Metodo pensato per comunicare a Notier il mancato invio dei documenti
	 * (analogo di err doc peppol CA dentro
	 * support.notier@regione.emilia-romagna.it).
	 * 
	 * @param textMessage
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendSimpleMessage(String textMessage) throws AddressException, MessagingException {
		Message message = new MimeMessage(getSessionForEmailSending());
		message.setFrom(new InternetAddress("test@test.it"));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("test.to@test.it"));
		message.setSubject("Test e-mail");

		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setContent(textMessage, "text/html");

		Multipart mp = new MimeMultipart();
		mp.addBodyPart(mbp);

		message.setContent(mp);

		Transport.send(message);
	}

	/**
	 * Recupera una Session.
	 * @return
	 */
	private Session getSessionForEmailSending() {
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", config.readSingleProperty("mail.smtp.auth"));
		prop.put("mail.smtp.starttls.enable", config.readSingleProperty("mail.smtp.starttls.enable"));
		prop.put("mail.smtp.host", "smtp.mailtrap.io");
		prop.put("mail.smtp.port", "25");
		prop.put("mail.smtp.ssl.trust", "smtp.mailtrap.io");

		return Session.getInstance(prop, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("username", "password");
			}
		});
	}

}
