package com.example.group3ma;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender extends AsyncTask<Void, Void, Void> {

    // IMPORTANT: Use an App Password if using Gmail, not your account password.
    // Go to Google Account Settings -> Security -> 2-Step Verification -> App Passwords.
    private static final String SENDER_EMAIL = "your-email@gmail.com"; 
    private static final String SENDER_PASSWORD = "your-app-password"; 

    private String email;
    private String subject;
    private String message;

    public EmailSender(String email, String subject, String message) {
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

        try {
            MimeMessage mm = new MimeMessage(session);
            mm.setFrom(new InternetAddress(SENDER_EMAIL));
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            mm.setSubject(subject);
            mm.setText(message);
            Transport.send(mm);
        } catch (MessagingException e) {
            Log.e("EmailSender", "Error sending email", e);
        }
        return null;
    }
}
