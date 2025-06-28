package com.notesSharingApp.notesSharingApp.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class emailService {

    @Autowired
    private JavaMailSender emailSender;


  public void sendVerificationCode(String to,int code) throws MessagingException {
      String subject = "Email Verification Code";
      String body =  "<html>"
              + "<body style=\"font-family: Arial, sans-serif;\">"
              + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
              + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
              + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
              + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
              + "<h3 style=\"color: #333;\">Verification Code:</h3>"
              + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + code + "</p>"
              + "</div>"
              + "</div>"
              + "</body>"
              + "</html>";

      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper messageHelper = new MimeMessageHelper(message,true);

      messageHelper.setTo(to);
      messageHelper.setSubject(subject);
      messageHelper.setText(body,true);

      emailSender.send(message);

  }

}
