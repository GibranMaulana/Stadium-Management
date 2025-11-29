package org.openjfx.util;

import org.openjfx.model.Booking;
import org.openjfx.model.BookingSeat;
import org.openjfx.model.Event;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test utility for email functionality
 * Run this to verify email configuration and test sending emails
 */
public class TestEmailService {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("   Email Service Configuration Test");
        System.out.println("===========================================\n");
        
        // Test 1: Email Configuration
        System.out.println("Test 1: Checking email configuration...");
        boolean configValid = MailUtils.testEmailConfiguration();
        
        if (!configValid) {
            System.err.println("\n❌ Email configuration test FAILED!");
            System.err.println("\nPlease check your .env file and ensure:");
            System.err.println("1. EMAIL_USERNAME is set to your Gmail address");
            System.err.println("2. EMAIL_PASSWORD is set to your Gmail App Password (not regular password)");
            System.err.println("3. You have enabled 2-Factor Authentication on your Google Account");
            System.err.println("\nSee EMAIL_SETUP.md for detailed instructions.");
            return;
        }
        
        System.out.println("✓ Email configuration is valid!\n");
        
        // Test 2: Create Mock Booking and Event
        System.out.println("Test 2: Creating test booking and event data...");
        
        Event mockEvent = createMockEvent();
        Booking mockBooking = createMockBooking();
        mockBooking.setBookingSeats(createMockBookingSeats());
        
        System.out.println("✓ Test data created\n");
        
        // Test 3: Send Test Confirmation Email
        System.out.println("Test 3: Sending test booking confirmation email...");
        System.out.println("To: " + mockBooking.getCustomerEmail());
        
        boolean confirmationSent = MailUtils.sendBookingConfirmation(mockBooking, mockEvent);
        
        if (confirmationSent) {
            System.out.println("✓ Booking confirmation email sent successfully!");
            System.out.println("  Check your inbox: " + mockBooking.getCustomerEmail());
        } else {
            System.err.println("✗ Failed to send booking confirmation email");
        }
        
        System.out.println();
        
        // Test 4: Send Test Cancellation Email
        System.out.println("Test 4: Sending test booking cancellation email...");
        
        boolean cancellationSent = MailUtils.sendBookingCancellation(mockBooking, mockEvent);
        
        if (cancellationSent) {
            System.out.println("✓ Booking cancellation email sent successfully!");
        } else {
            System.err.println("✗ Failed to send booking cancellation email");
        }
        
        // Summary
        System.out.println("\n===========================================");
        System.out.println("   Test Summary");
        System.out.println("===========================================");
        System.out.println("Configuration Test: " + (configValid ? "✓ PASSED" : "✗ FAILED"));
        System.out.println("Confirmation Email: " + (confirmationSent ? "✓ SENT" : "✗ FAILED"));
        System.out.println("Cancellation Email: " + (cancellationSent ? "✓ SENT" : "✗ FAILED"));
        System.out.println("===========================================\n");
        
        if (configValid && confirmationSent && cancellationSent) {
            System.out.println("🎉 All tests PASSED! Email service is working correctly.");
            System.out.println("\nNext steps:");
            System.out.println("1. Check your email inbox for the test emails");
            System.out.println("2. Verify the email templates look correct");
            System.out.println("3. Update EMAIL_USERNAME in .env to use your actual email");
            System.out.println("4. Your booking system will now send emails automatically!");
        } else {
            System.err.println("⚠️  Some tests failed. Please check the errors above.");
        }
    }
    
    /**
     * Create a mock event for testing
     */
    private static Event createMockEvent() {
        Event event = new Event();
        event.setId(1);
        event.setEventName("Arsenal vs Manchester United");
        event.setEventType("Football Match");
        event.setEventDate(LocalDate.now().plusDays(7));
        event.setEventTime(LocalTime.of(19, 30));
        event.setDescription("Premier League Match - Emirates Stadium");
        event.setStatus("UPCOMING");
        event.setTotalSeats(60000);
        event.setBookedSeats(25000);
        
        return event;
    }
    
    /**
     * Create a mock booking for testing
     */
    private static Booking createMockBooking() {
        Booking booking = new Booking();
        booking.setBookingId(999);
        booking.setEventId(1);
        booking.setBookingNumber("BK-TEST-" + System.currentTimeMillis());
        booking.setCustomerName("Test Customer");
        
        // IMPORTANT: Change this to YOUR email address to receive test emails
        String testEmail = System.getenv("EMAIL_USERNAME"); // Uses same email as sender for testing
        if (testEmail == null || testEmail.isEmpty()) {
            testEmail = "test@example.com"; // Fallback
        }
        
        booking.setCustomerEmail(testEmail);
        booking.setCustomerPhone("+62 812-3456-7890");
        booking.setTotalSeats(2);
        booking.setTotalPrice(500000.00);
        booking.setBookingStatus("CONFIRMED");
        booking.setBookingDate(new Timestamp(System.currentTimeMillis()));
        booking.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        booking.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        return booking;
    }
    
    /**
     * Create mock booking seats for testing
     */
    private static List<BookingSeat> createMockBookingSeats() {
        List<BookingSeat> seats = new ArrayList<>();
        
        BookingSeat seat1 = new BookingSeat();
        seat1.setBookingSeatId(1);
        seat1.setBookingId(999);
        seat1.setEventId(1);
        seat1.setSectionId(1);
        seat1.setSectionName("North Stand");
        seat1.setRowNumber("A");
        seat1.setSeatNumber(15);
        seat1.setPrice(250000.00);
        seat1.setStatus("CONFIRMED");
        
        BookingSeat seat2 = new BookingSeat();
        seat2.setBookingSeatId(2);
        seat2.setBookingId(999);
        seat2.setEventId(1);
        seat2.setSectionId(1);
        seat2.setSectionName("North Stand");
        seat2.setRowNumber("A");
        seat2.setSeatNumber(16);
        seat2.setPrice(250000.00);
        seat2.setStatus("CONFIRMED");
        
        seats.add(seat1);
        seats.add(seat2);
        
        return seats;
    }
}
