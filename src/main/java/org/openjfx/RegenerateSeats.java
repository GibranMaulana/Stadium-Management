package org.openjfx;

import org.openjfx.service.SeatGenerationService;

/**
 * Utility to regenerate all seats in the database
 * Run this once to fix the current database seats issue
 */
public class RegenerateSeats {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("SEAT REGENERATION UTILITY");
        System.out.println("========================================");
        System.out.println("This will regenerate all seats for tribune sections");
        System.out.println("to match their configured dimensions.");
        System.out.println("");
        
        SeatGenerationService service = new SeatGenerationService();
        
        try {
            service.regenerateAllSeats();
            System.out.println("\n✅ SUCCESS! All seats have been regenerated.");
            System.out.println("You can now use the booking system with all seats available.");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERROR: Failed to regenerate seats");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
