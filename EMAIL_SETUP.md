# Email Configuration Guide

This guide explains how to set up email functionality for sending booking confirmations and cancellations.

## Overview

The Stadium Management System can automatically send emails to customers when:
- ✅ A booking is successfully created (confirmation email)
- ❌ A booking is cancelled (cancellation email)

## Features

### Booking Confirmation Email
- Professional HTML email template
- Booking details (booking number, event name, date, time)
- Seat information (section, row, seat number)
- Total price and payment details
- Important instructions for the customer
- Responsive design for mobile and desktop

### Booking Cancellation Email
- Cancellation confirmation
- Refund information (if applicable)
- Cancelled booking details

## Email Service Architecture

```
BookingService (service/)
    ├── createBooking() → sendBookingConfirmationEmailAsync()
    ├── cancelBooking() → sendBookingCancellationEmailAsync()
    └── Uses MailUtils for actual sending

MailUtils (util/)
    ├── sendBookingConfirmation()
    ├── sendBookingCancellation()
    ├── buildBookingConfirmationEmail()
    └── buildBookingCancellationEmail()
```

## Setup Instructions

### 1. Gmail Configuration (Recommended)

#### Step 1: Enable 2-Factor Authentication
1. Go to your Google Account settings: https://myaccount.google.com
2. Navigate to **Security**
3. Enable **2-Step Verification**

#### Step 2: Generate App Password
1. Go to https://myaccount.google.com/apppasswords
2. Select app: **Mail**
3. Select device: **Other (Custom name)** → Enter "Stadium Management"
4. Click **Generate**
5. Copy the 16-character password (format: `xxxx xxxx xxxx xxxx`)

#### Step 3: Update .env File
Edit your `.env` file:

```env
# Email Configuration
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=abcd efgh ijkl mnop
EMAIL_FROM=noreply@stadiummanagement.com
```

### 2. Other Email Providers

#### Outlook/Hotmail
```env
# In MailUtils.java, change:
SMTP_HOST=smtp-mail.outlook.com
SMTP_PORT=587

# In .env:
EMAIL_USERNAME=your-email@outlook.com
EMAIL_PASSWORD=your-password
```

#### Yahoo Mail
```env
# In MailUtils.java, change:
SMTP_HOST=smtp.mail.yahoo.com
SMTP_PORT=587

# In .env:
EMAIL_USERNAME=your-email@yahoo.com
EMAIL_PASSWORD=your-app-password
```

#### Custom SMTP Server
```env
# In MailUtils.java, change:
SMTP_HOST=your-smtp-server.com
SMTP_PORT=587

# In .env:
EMAIL_USERNAME=your-username
EMAIL_PASSWORD=your-password
```

## Testing Email Configuration

### Test 1: Configuration Test
Add this to your main application or a test class:

```java
import org.openjfx.util.MailUtils;

public class TestEmail {
    public static void main(String[] args) {
        boolean success = MailUtils.testEmailConfiguration();
        
        if (success) {
            System.out.println("✓ Email configuration is valid!");
        } else {
            System.out.println("✗ Email configuration failed. Check your credentials.");
        }
    }
}
```

### Test 2: Send Test Booking Email
Create a test booking and check if the email is sent:

```java
// Create a test booking
Booking booking = new Booking();
booking.setBookingNumber("TEST-001");
booking.setCustomerName("Test Customer");
booking.setCustomerEmail("test@example.com");
booking.setTotalSeats(2);
booking.setTotalPrice(200000);

Event event = eventService.getEventById(1);

// Send test email
boolean sent = MailUtils.sendBookingConfirmation(booking, event);
System.out.println("Email sent: " + sent);
```

## Troubleshooting

### Problem: Authentication Failed
**Solution:**
- Verify your Gmail credentials are correct
- Make sure you're using an App Password, not your regular Gmail password
- Check that 2-Factor Authentication is enabled on your Google Account

### Problem: Connection Timeout
**Solution:**
- Check your internet connection
- Verify SMTP host and port are correct
- Check if your firewall/antivirus is blocking port 587
- Try using port 465 with SSL instead

### Problem: Email Not Received
**Solution:**
- Check spam/junk folder
- Verify the recipient email address is correct
- Check email logs in the console for errors
- Test with `testEmailConfiguration()` first

### Problem: "javax.mail package not accessible"
**Solution:**
- Make sure JavaMail dependency is in your `pom.xml`
- Run `mvn clean install` to download dependencies
- Reload Maven project in VS Code

## Email Template Customization

### Modify Email Content
Edit `/src/main/java/org/openjfx/util/MailUtils.java`:

```java
private static String buildBookingConfirmationEmail(Booking booking, Event event) {
    // Customize HTML template here
    // Change colors, layout, content, etc.
}
```

### Change From Address
Edit `.env`:

```env
EMAIL_FROM=your-custom-address@yourdomain.com
```

### Change Email Subject
In `MailUtils.java`:

```java
message.setSubject("Your Custom Subject - " + event.getEventName());
```

## Security Best Practices

### ✅ DO:
- Use App Passwords for Gmail (never use your actual Gmail password)
- Keep `.env` file in `.gitignore` (already configured)
- Use environment variables for sensitive data
- Enable 2-Factor Authentication on your email account
- Regularly rotate your App Passwords

### ❌ DON'T:
- Commit credentials to Git
- Share your `.env` file
- Use your main email password directly in the code
- Hardcode email credentials in source files

## Production Deployment

For production, consider:

1. **Use a dedicated email service:**
   - SendGrid
   - AWS SES (Simple Email Service)
   - Mailgun
   - Postmark

2. **Set environment variables on server:**
   ```bash
   export EMAIL_USERNAME=your-email@gmail.com
   export EMAIL_PASSWORD=your-app-password
   export EMAIL_FROM=noreply@stadiummanagement.com
   ```

3. **Monitor email delivery:**
   - Track bounce rates
   - Log all email sends
   - Implement retry logic for failed sends

## Async Email Sending

Emails are sent asynchronously to avoid blocking the booking process:

```java
// Email is sent in a separate thread
private void sendBookingConfirmationEmailAsync(Booking booking) {
    new Thread(() -> {
        // Email sending logic
    }).start();
}
```

This ensures:
- ✅ Fast booking creation (doesn't wait for email)
- ✅ Better user experience
- ✅ No blocking if email server is slow

## Support

If you need help:
1. Check the console output for error messages
2. Verify all dependencies are installed (`mvn dependency:tree`)
3. Test email configuration with `testEmailConfiguration()`
4. Check Gmail App Password setup: https://support.google.com/accounts/answer/185833

## Dependencies

Required Maven dependencies (already in `pom.xml`):

```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>

<dependency>
    <groupId>jakarta.activation</groupId>
    <artifactId>jakarta.activation-api</artifactId>
    <version>2.1.0</version>
</dependency>
```

---

**Last Updated:** November 29, 2025
**Version:** 1.0.0
