package com.rest.publicpoll.contact;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("/contact")
public class ContactRestfulService {
	@Path("/sendEmail")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	//Returns ok if successful, error otherwise.
	public Response sendEmailService(String json) {
		String publicPollEmail = "alexander.w.aghili@gmail.com";
		JSONObject jo = new JSONObject(json);
		String senderEmail = jo.getString("email");
		String report = jo.getString("report");
		
		String response = sendEmail(senderEmail, publicPollEmail, report);
		return Response.status(201).entity(response).build();
		
	}
	
	public String sendEmail(String sender, String recipiant, String msg) {
		String password = "**********";
		
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(recipiant, password);
                    }
                });

		String response = "";
		try {
	         // Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
			message.setFrom(new InternetAddress(sender));


	         // Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipiant));

			message.setHeader("X-Priority", "1");
	         // Set Subject: header field
			message.setSubject("Bug Report");

			// Now set the actual message
			message.setText("Sender: " + sender + "\n" + msg);

			// Send message
			Transport.send(message);
			response = "ok";
		} catch (MessagingException mex) {
			response = "error";
			mex.printStackTrace();
		}
		return response;
	}
}
