package com.notesSharingApp.notesSharingApp.Service;

import com.notesSharingApp.notesSharingApp.model.Note;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Async("asyncTask")
public class EmailService {

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
      System.out.println("Email Thread name = " + Thread.currentThread().getName());

  }
  public void sendRemarkNotification(String name,String coursename,String to,String body) throws MessagingException {
      String subject = "Notes Remark Request";
      String html = "<html lang=\"en\">\n" +
              "<head>\n" +
              "  <meta charset=\"UTF-8\">\n" +
              "  <title>Update Notes Request</title>\n" +
              "</head>\n" +
              "<body style=\"background: #f4f6fa; margin: 0; padding: 0; font-family: Arial, sans-serif;\">\n" +
              "  <div style=\"background: #fff; max-width: 600px; margin: 40px auto; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: hidden;\">\n" +
              "    <div style=\"background: #2563eb; color: #fff; padding: 24px 32px; text-align: center;\">\n" +
              "      <h2 style=\"margin: 0; font-size: 24px;\">Action Required: Update Your Uploaded Notes</h2>\n" +
              "    </div>\n" +
              "    <div style=\"padding: 32px; color: #333; line-height: 1.6;\">\n" +
              "      <p style=\"margin-top: 0;\">Dear <strong>"+name+"</strong>,</p>\n" +
              "      <p>\n" +
              "        Thank you for submitting your notes for <strong>"+coursename+"</strong>. After reviewing your submission, we have identified some issues that require your attention:\n" +
              "      </p>\n" +
              "      <ul style=\"margin: 16px 0;\">\n" +
              "       <p>"+body+"</p>" +
              "        <!-- Add more list items as needed -->\n" +
              "      </ul>\n" +
              "      <p>\n" +
              "        Please review and update your notes accordingly.\n" +
              "      </p>\n" +
              "      <a href=\"[Link to Update Page]\" \n" +
              "         style=\"display: inline-block; background: #2563eb; color: #fff !important; padding: 12px 28px; margin: 24px 0; border-radius: 4px; text-decoration: none; font-weight: bold; font-size: 16px;\">\n" +
              "         Update My Notes\n" +
              "      </a>\n" +
              "      <p>\n" +
              "        If you have any questions or need clarification, please reply to this email.\n" +
              "      </p>\n" +
              "      <p style=\"margin-bottom: 0;\">Best regards,<br>\n" +
              "      Admin Team</p>\n" +
              "    </div>\n" +
              "    <div style=\"background: #f0f0f0; color: #888; text-align: center; padding: 16px 32px; font-size: 13px;\">\n" +
              "      &copy; 2025 [Your Web App Name]. All rights reserved.\n" +
              "    </div>\n" +
              "  </div>\n" +
              "</body>\n" +
              "</html>\n";

      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper messageHelper = new MimeMessageHelper(message,true);

      messageHelper.setTo(to);
      messageHelper.setSubject(subject);
      messageHelper.setText(html,true);
      emailSender.send(message);

  }
  public void sendNoteRemovalEmail(Note noteToBeRemoved,String reason) throws MessagingException {
      String subject = "Note Removed";
      String s = "<html lang=\"en\">\n" +
              "<head>\n" +
              "  <meta charset=\"UTF-8\">\n" +
              "  <title>Notes Removal Notification</title>\n" +
              "</head>\n" +
              "<body style=\"background: #f4f6fa; margin: 0; padding: 0; font-family: Arial, sans-serif;\">\n" +
              "  <div style=\"background: #fff; max-width: 600px; margin: 40px auto; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: hidden;\">\n" +
              "    <div style=\"background: #dc2626; color: #fff; padding: 24px 32px; text-align: center;\">\n" +
              "      <h2 style=\"margin: 0; font-size: 24px;\">Important: Your Notes Have Been Removed</h2>\n" +
              "    </div>\n" +
              "    <div style=\"padding: 32px; color: #333; line-height: 1.6;\">\n" +
              "      <p style=\"margin-top: 0;\">Dear <strong>" + noteToBeRemoved.getCreatedBy().getUsername() + "</strong>,</p>\n" +
              "      <p>\n" +
              "        We regret to inform you that your notes for <strong>" + noteToBeRemoved.getSubject().getSubjectName() + "</strong> have been removed from our platform due to the following reason(s):\n" +
              "      </p>\n" +
              "      <ul style=\"margin: 16px 0;\">\n" +
              "        <p>" + reason + "</p>\n" +
              "      </ul>\n" +
              "      <p>\n" +
              "        Unfortunately, the removed notes are no longer visible to other students. If you wish, you can upload a revised version that complies with our guidelines.\n" +
              "      </p>\n" +
//              "      <a href=\"[Link to Upload Page]\" \n" +
//              "         style=\"display: inline-block; background: #2563eb; color: #fff !important; padding: 12px 28px; margin: 24px 0; border-radius: 4px; text-decoration: none; font-weight: bold; font-size: 16px;\">\n" +
//              "         Upload New Notes\n" +
//              "      </a>\n" +
              "      <p style=\"margin-bottom: 0;\">Best regards,<br>\n" +
              "      Admin Team</p>\n" +
              "    </div>\n" +
              "    <div style=\"background: #f0f0f0; color: #888; text-align: center; padding: 16px 32px; font-size: 13px;\">\n" +
              "      &copy; 2025 Study Share. All rights reserved.\n" +
              "    </div>\n" +
              "  </div>\n" +
              "</body>\n" +
              "</html>\n";

      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper messageHelper = new MimeMessageHelper(message,true);

      messageHelper.setTo(noteToBeRemoved.getCreatedBy().getUniversityEmail());
      messageHelper.setSubject(subject);
      messageHelper.setText(s,true);
      emailSender.send(message);

  }
  public void sendForgotPasswordLink(String link,String to) throws MessagingException {
      String subject = "Forget Password link";
      String forgotPasswordTemplate =
              "<html>" +
                      "<body style=\"font-family: Arial, sans-serif; background-color: #f9f9f9; margin: 0; padding: 0;\">" +
                      "<div style=\"max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">" +
                      "<h2 style=\"color: #333;\">Reset Your Password</h2>" +
                      "<p style=\"font-size: 16px; color: #555;\">We received a request to reset your password. Click the button below to set a new password.</p>" +
                      "<div style=\"text-align: center; margin: 30px 0;\">" +
                      "<a href=\"http://localhost:5173/changePassword?reset_token=" + link + "&email="+to+"\"  " +
                      "style=\"background-color: #007bff; color: #ffffff; padding: 12px 20px; text-decoration: none; font-size: 16px; border-radius: 5px; display: inline-block;\">" +
                      "Reset Password" +
                      "</a>" +
                      "</div>" +
                      "<hr style=\"margin: 20px 0; border: none; border-top: 1px solid #eee;\"/>" +
                      "<p style=\"font-size: 12px; color: #aaa; text-align: center;\">&copy; 2025 Study Share. All rights reserved.</p>" +
                      "</div>" +
                      "</body>" +
                      "</html>";

      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper messageHelper = new MimeMessageHelper(message,true);

      messageHelper.setTo(to);
      messageHelper.setSubject(subject);
      messageHelper.setText(forgotPasswordTemplate,true);
      emailSender.send(message);

  }
}
