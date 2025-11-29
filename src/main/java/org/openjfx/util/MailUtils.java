package org.openjfx.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.openjfx.model.Booking;
import org.openjfx.model.BookingSeat;
import org.openjfx.model.Event;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Utility class for sending emails
 * Handles booking confirmation emails and other email notifications
 */
public class MailUtils {
    
    // Load environment variables from .env file
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .ignoreIfMissing()
            .load();
    
    // Email configuration - Loaded from .env file
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = dotenv.get("EMAIL_USERNAME");
    private static final String EMAIL_PASSWORD = dotenv.get("EMAIL_PASSWORD");
    private static final String FROM_EMAIL = dotenv.get("EMAIL_FROM", "noreply@stadiummanagement.com");
    private static final String FROM_NAME = "Stadium Management System";
    
    /**
     * Send booking confirmation email to customer
     */
    public static boolean sendBookingConfirmation(Booking booking, Event event) {
        try {
            // Get email session
            Session session = getEmailSession();
            
            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(booking.getCustomerEmail()));
            message.setSubject("Booking Confirmation - " + event.getEventName());
            
            // Create HTML email content
            String emailContent = buildBookingConfirmationEmail(booking, event);
            message.setContent(emailContent, "text/html; charset=utf-8");
            
            // Send email
            Transport.send(message);
            
            System.out.println("✓ Booking confirmation email sent to: " + booking.getCustomerEmail());
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ Failed to send booking confirmation email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send booking cancellation email to customer
     */
    public static boolean sendBookingCancellation(Booking booking, Event event) {
        try {
            Session session = getEmailSession();
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(booking.getCustomerEmail()));
            message.setSubject("Booking Cancellation - " + event.getEventName());
            
            String emailContent = buildBookingCancellationEmail(booking, event);
            message.setContent(emailContent, "text/html; charset=utf-8");
            
            Transport.send(message);
            
            System.out.println("✓ Booking cancellation email sent to: " + booking.getCustomerEmail());
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ Failed to send booking cancellation email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Configure and return email session
     */
    private static Session getEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });
    }
    
    /**
     * Build HTML email for booking confirmation
     */
    private static String buildBookingConfirmationEmail(Booking booking, Event event) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
            .append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>")
            .append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>")
            
            // Header
            .append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>")
            .append("<h1 style='margin: 0; font-size: 28px;'>🎟️ Booking Confirmed!</h1>")
            .append("</div>")
            
            // Body
            .append("<div style='padding: 30px; background: #f9f9f9;'>")
            .append("<p style='font-size: 16px;'>Dear <strong>").append(booking.getCustomerName()).append("</strong>,</p>")
            .append("<p>Your booking has been confirmed! We're excited to see you at the event.</p>")
            
            // Booking Details
            .append("<div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>")
            .append("<h2 style='color: #667eea; margin-top: 0; border-bottom: 2px solid #667eea; padding-bottom: 10px;'>Booking Details</h2>")
            .append("<table style='width: 100%; border-collapse: collapse;'>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Booking Number:</td><td style='padding: 8px 0;'>").append(booking.getBookingNumber()).append("</td></tr>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Event:</td><td style='padding: 8px 0;'>").append(event.getEventName()).append("</td></tr>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Date:</td><td style='padding: 8px 0;'>").append(event.getEventDate().format(dateFormatter)).append("</td></tr>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Time:</td><td style='padding: 8px 0;'>").append(event.getEventTime().format(timeFormatter)).append("</td></tr>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Total Seats:</td><td style='padding: 8px 0;'>").append(booking.getTotalSeats()).append("</td></tr>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Total Price:</td><td style='padding: 8px 0; color: #27ae60; font-size: 18px; font-weight: bold;'>Rp ").append(String.format("%,.0f", booking.getTotalPrice())).append("</td></tr>")
            .append("</table>")
            .append("</div>");
        
        // Seat Information
        if (booking.getBookingSeats() != null && !booking.getBookingSeats().isEmpty()) {
            html.append("<div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>")
                .append("<h3 style='color: #667eea; margin-top: 0;'>Your Seats</h3>")
                .append("<ul style='list-style: none; padding: 0;'>");
            
            for (BookingSeat seat : booking.getBookingSeats()) {
                html.append("<li style='padding: 5px 0; border-bottom: 1px solid #eee;'>")
                    .append("📍 ").append(seat.getSectionName());
                
                if (seat.getRowNumber() != null && !seat.getRowNumber().isEmpty()) {
                    html.append(" - Row ").append(seat.getRowNumber())
                        .append(", Seat ").append(seat.getSeatNumber());
                }
                html.append("</li>");
            }
            
            html.append("</ul></div>");
        }
        
        // Important Information
        html.append("<div style='background: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107;'>")
            .append("<h3 style='margin-top: 0; color: #856404;'>⚠️ Important Information</h3>")
            .append("<ul style='margin: 0; padding-left: 20px; color: #856404;'>")
            .append("<li>Please arrive at least 30 minutes before the event starts</li>")
            .append("<li>Bring a valid ID for verification</li>")
            .append("<li>Show this email or booking number at the entrance</li>")
            .append("<li>Gates open 1 hour before the event</li>")
            .append("</ul></div>")
            
            // Footer
            .append("<p style='text-align: center; margin-top: 30px; color: #666;'>")
            .append("If you have any questions, please contact us at support@stadiummanagement.com")
            .append("</p>")
            .append("<p style='text-align: center; margin-top: 20px; font-size: 12px; color: #999;'>")
            .append("This is an automated message, please do not reply to this email.")
            .append("</p>")
            .append("</div>")
            
            .append("</div></body></html>");
        
        return html.toString();
    }
    
    /**
     * Build HTML email for booking cancellation
     */
    private static String buildBookingCancellationEmail(Booking booking, Event event) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
            .append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>")
            .append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>")
            
            // Header
            .append("<div style='background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>")
            .append("<h1 style='margin: 0; font-size: 28px;'>Booking Cancelled</h1>")
            .append("</div>")
            
            // Body
            .append("<div style='padding: 30px; background: #f9f9f9;'>")
            .append("<p style='font-size: 16px;'>Dear <strong>").append(booking.getCustomerName()).append("</strong>,</p>")
            .append("<p>Your booking has been cancelled as requested.</p>")
            
            // Booking Details
            .append("<div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0;'>")
            .append("<h2 style='color: #e74c3c; margin-top: 0;'>Cancelled Booking Details</h2>")
            .append("<table style='width: 100%;'>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Booking Number:</td><td style='padding: 8px 0;'>").append(booking.getBookingNumber()).append("</td></tr>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Event:</td><td style='padding: 8px 0;'>").append(event.getEventName()).append("</td></tr>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Date:</td><td style='padding: 8px 0;'>").append(event.getEventDate().format(dateFormatter)).append("</td></tr>")
            .append("<tr><td style='padding: 8px 0; font-weight: bold;'>Total Amount:</td><td style='padding: 8px 0;'>Rp ").append(String.format("%,.0f", booking.getTotalPrice())).append("</td></tr>")
            .append("</table>")
            .append("</div>")
            
            .append("<p>If you did not request this cancellation, please contact us immediately.</p>")
            .append("<p style='text-align: center; margin-top: 30px; color: #666;'>")
            .append("Contact us at support@stadiummanagement.com for any questions.")
            .append("</p>")
            .append("</div>")
            
            .append("</div></body></html>");
        
        return html.toString();
    }
    
    /**
     * Test email configuration
     */
    public static boolean testEmailConfiguration() {
        try {
            Session session = getEmailSession();
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            System.out.println("✓ Email configuration is valid");
            return true;
        } catch (Exception e) {
            System.err.println("✗ Email configuration failed: " + e.getMessage());
            return false;
        }
    }
}
