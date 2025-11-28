package org.openjfx.service;

import org.openjfx.model.InventoryItem;
import org.openjfx.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for generating reports and analytics
 * Provides data for charts and statistics
 */
public class ReportService {
    
    private final InventoryService inventoryService;
    private final InventoryPurchaseService purchaseService;
    private final EventExpenseService eventExpenseService;
    
    public ReportService() {
        this.inventoryService = new InventoryService();
        this.purchaseService = new InventoryPurchaseService();
        this.eventExpenseService = new EventExpenseService();
    }

    // Simple refresh listener support so UI components can refresh when underlying data changes
    private static final java.util.List<Runnable> refreshListeners = new java.util.ArrayList<>();

    public static void addRefreshListener(Runnable r) {
        synchronized (refreshListeners) { refreshListeners.add(r); }
    }

    public static void removeRefreshListener(Runnable r) {
        synchronized (refreshListeners) { refreshListeners.remove(r); }
    }

    public static void notifyRefreshListeners() {
        java.util.List<Runnable> copy;
        synchronized (refreshListeners) { copy = new java.util.ArrayList<>(refreshListeners); }
        for (Runnable r : copy) {
            try { r.run(); } catch (Exception ignored) {}
        }
    }

    /**
     * Get total revenue between two dates (inclusive)
     */
    public double getRevenueForPeriod(LocalDate start, LocalDate end) {
        String query = "SELECT ISNULL(SUM(TotalPrice),0) FROM Bookings WHERE BookingStatus = 'CONFIRMED' " +
                       "AND CAST(BookingDate AS DATE) BETWEEN ? AND ?";
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, start.toString());
            stmt.setString(2, end.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }

        } catch (SQLException e) {
            System.err.println("Error getting revenue for period: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Get total revenue for a single event
     */
    public double getRevenueForEvent(int eventId) {
        String query = "SELECT ISNULL(SUM(TotalPrice),0) FROM Bookings WHERE BookingStatus = 'CONFIRMED' AND EventID = ?";
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }

        } catch (SQLException e) {
            System.err.println("Error getting revenue for event: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Total expenses (purchases) in a period
     */
    public double getTotalExpenses(LocalDate start, LocalDate end) {
        double purchases = purchaseService.getTotalExpensesInPeriod(start, end);
        double eventExpenses = eventExpenseService.getTotalExpensesInPeriod(start, end);
        return purchases + eventExpenses;
    }

    /**
     * Profit for a period (revenue - expenses)
     */
    public double getProfitForPeriod(LocalDate start, LocalDate end) {
        double revenue = getRevenueForPeriod(start, end);
        double expenses = getTotalExpenses(start, end);
        return revenue - expenses;
    }

    /**
     * Profit for an event (event revenue - expenses attributed to event)
     */
    public double getProfitForEvent(int eventId) {
        double revenue = getRevenueForEvent(eventId);
        double purchases = purchaseService.getTotalExpensesForEvent(eventId);
        double eventExpenses = eventExpenseService.getTotalExpensesForEvent(eventId);
        double expenses = purchases + eventExpenses;
        return revenue - expenses;
    }
    
    /**
     * Get sales revenue per event
     * @return Map of Event Name to Total Revenue
     */
    public Map<String, Double> getSalesPerEvent() {
        Map<String, Double> salesData = new LinkedHashMap<>();
        String query = "SELECT EventName, ISNULL(TotalRevenue, 0) as Revenue " +
                      "FROM vw_SalesPerEvent " +
                      "ORDER BY TotalRevenue DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String eventName = rs.getString("EventName");
                double revenue = rs.getDouble("Revenue");
                salesData.put(eventName, revenue);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting sales per event!");
            e.printStackTrace();
        }
        
        return salesData;
    }
    
    /**
     * Get tickets sold per event
     * @return Map of Event Name to Tickets Sold Count
     */
    public Map<String, Integer> getTicketsSoldPerEvent() {
        Map<String, Integer> ticketsData = new LinkedHashMap<>();
        String query = "SELECT EventName, ISNULL(TotalTicketsSold, 0) as TicketsSold " +
                      "FROM vw_SalesPerEvent " +
                      "ORDER BY TotalTicketsSold DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String eventName = rs.getString("EventName");
                int ticketsSold = rs.getInt("TicketsSold");
                ticketsData.put(eventName, ticketsSold);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting tickets sold per event!");
            e.printStackTrace();
        }
        
        return ticketsData;
    }
    
    /**
     * Get bookings count per event
     * @return Map of Event Name to Booking Count
     */
    public Map<String, Integer> getBookingsPerEvent() {
        Map<String, Integer> bookingsData = new LinkedHashMap<>();
        String query = "SELECT EventName, ISNULL(TotalBookings, 0) as BookingCount " +
                      "FROM vw_SalesPerEvent " +
                      "ORDER BY TotalBookings DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String eventName = rs.getString("EventName");
                int bookingCount = rs.getInt("BookingCount");
                bookingsData.put(eventName, bookingCount);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting bookings per event!");
            e.printStackTrace();
        }
        
        return bookingsData;
    }
    
    /**
     * Get inventory report (low stock items)
     * @return List of items with low stock
     */
    public List<InventoryItem> getInventoryReport() {
        return inventoryService.getLowStockItems();
    }
    
    /**
     * Get total revenue across all events
     * @return Total revenue amount
     */
    public double getTotalRevenue() {
        String query = "SELECT ISNULL(SUM(TotalRevenue), 0) as GrandTotal FROM vw_SalesPerEvent";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("GrandTotal");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total revenue!");
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Get total tickets sold across all events
     * @return Total tickets sold count
     */
    public int getTotalTicketsSold() {
        String query = "SELECT ISNULL(SUM(TotalTicketsSold), 0) as GrandTotal FROM vw_SalesPerEvent";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("GrandTotal");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total tickets sold!");
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get monthly revenue trend
     * @return Map of Month-Year to Revenue
     */
    public Map<String, Double> getMonthlyRevenueTrend() {
        Map<String, Double> monthlyData = new LinkedHashMap<>();
        String query = "SELECT FORMAT(e.EventDate, 'MMM yyyy') as Month, " +
                      "ISNULL(SUM(es.Price), 0) as Revenue " +
                      "FROM Events e " +
                      "LEFT JOIN EventSections es ON e.EventID = es.EventID " +
                      "LEFT JOIN Bookings b ON e.EventID = b.EventID " +
                      "LEFT JOIN BookingSeats bs ON b.BookingID = bs.BookingID AND bs.SectionID = es.SectionID " +
                      "GROUP BY FORMAT(e.EventDate, 'MMM yyyy'), YEAR(e.EventDate), MONTH(e.EventDate) " +
                      "ORDER BY YEAR(e.EventDate), MONTH(e.EventDate)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String month = rs.getString("Month");
                double revenue = rs.getDouble("Revenue");
                monthlyData.put(month, revenue);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting monthly revenue trend!");
            e.printStackTrace();
        }
        
        return monthlyData;
    }
    
    /**
     * Get event statistics summary
     * @return Map with various event statistics
     */
    public Map<String, Object> getEventStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        String query = "SELECT " +
                      "COUNT(DISTINCT e.EventID) as TotalEvents, " +
                      "COUNT(DISTINCT b.BookingID) as TotalBookings, " +
                      "COUNT(bs.SeatID) as TotalTicketsSold, " +
                      "ISNULL(SUM(es.Price), 0) as TotalRevenue, " +
                      "ISNULL(AVG(es.Price), 0) as AvgTicketPrice " +
                      "FROM Events e " +
                      "LEFT JOIN EventSections es ON e.EventID = es.EventID " +
                      "LEFT JOIN Bookings b ON e.EventID = b.EventID " +
                      "LEFT JOIN BookingSeats bs ON b.BookingID = bs.BookingID AND bs.SectionID = es.SectionID";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                stats.put("totalEvents", rs.getInt("TotalEvents"));
                stats.put("totalBookings", rs.getInt("TotalBookings"));
                stats.put("totalTicketsSold", rs.getInt("TotalTicketsSold"));
                stats.put("totalRevenue", rs.getDouble("TotalRevenue"));
                stats.put("avgTicketPrice", rs.getDouble("AvgTicketPrice"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting event statistics!");
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Get section popularity (which sections sell most)
     * @return Map of Section Name to Tickets Sold
     */
    public Map<String, Integer> getSectionPopularity() {
        Map<String, Integer> sectionData = new LinkedHashMap<>();
        String query = "SELECT s.Name as SectionName, COUNT(bs.SeatID) as TicketsSold " +
                      "FROM Sections s " +
                      "LEFT JOIN BookingSeats bs ON s.SectionID = bs.SectionID " +
                      "GROUP BY s.SectionID, s.Name " +
                      "ORDER BY COUNT(bs.SeatID) DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String sectionName = rs.getString("SectionName");
                int ticketsSold = rs.getInt("TicketsSold");
                sectionData.put(sectionName, ticketsSold);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting section popularity!");
            e.printStackTrace();
        }
        
        return sectionData;
    }
}
