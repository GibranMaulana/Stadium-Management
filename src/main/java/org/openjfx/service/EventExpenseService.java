package org.openjfx.service;

import org.openjfx.model.EventExpense;
import org.openjfx.util.DatabaseUtil;
import org.openjfx.service.ReportService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to manage Event-level expenses
 */
public class EventExpenseService {

    /**
     * Add a new expense record
     */
    public boolean addExpense(EventExpense e) {
        String sql = "INSERT INTO EventExpenses (EventID, ExpenseType, ItemID, Quantity, UnitCost, TotalCost, Notes, CreatedAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getEventId());
            stmt.setString(2, e.getExpenseType());
            if (e.getItemId() != null) stmt.setInt(3, e.getItemId()); else stmt.setNull(3, java.sql.Types.INTEGER);
            if (e.getQuantity() != null) stmt.setInt(4, e.getQuantity()); else stmt.setNull(4, java.sql.Types.INTEGER);
            if (e.getUnitCost() != null) stmt.setDouble(5, e.getUnitCost()); else stmt.setNull(5, java.sql.Types.DECIMAL);
            stmt.setDouble(6, e.getTotalCost());
            if (e.getNotes() != null) stmt.setString(7, e.getNotes()); else stmt.setNull(7, java.sql.Types.NVARCHAR);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                // notify listeners so UI can refresh charts/reports
                ReportService.notifyRefreshListeners();
                return true;
            }
            return false;

        } catch (SQLException ex) {
            System.err.println("Error adding event expense: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Get expenses for an event
     */
    public List<EventExpense> getExpensesForEvent(int eventId) {
        List<EventExpense> list = new ArrayList<>();
        String sql = "SELECT ExpenseID, EventID, ExpenseType, ItemID, Quantity, UnitCost, TotalCost, Notes, CreatedAt " +
                     "FROM EventExpenses WHERE EventID = ? ORDER BY CreatedAt DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EventExpense e = new EventExpense();
                e.setExpenseId(rs.getInt("ExpenseID"));
                e.setEventId(rs.getInt("EventID"));
                e.setExpenseType(rs.getString("ExpenseType"));
                int itemId = rs.getInt("ItemID"); if (!rs.wasNull()) e.setItemId(itemId);
                int qty = rs.getInt("Quantity"); if (!rs.wasNull()) e.setQuantity(qty);
                double u = rs.getDouble("UnitCost"); if (!rs.wasNull()) e.setUnitCost(u);
                e.setTotalCost(rs.getDouble("TotalCost"));
                e.setNotes(rs.getString("Notes"));
                java.sql.Timestamp ts = rs.getTimestamp("CreatedAt");
                if (ts != null) e.setCreatedAt(ts.toLocalDateTime());
                list.add(e);
            }

        } catch (SQLException ex) {
            System.err.println("Error fetching event expenses: " + ex.getMessage());
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Sum total expenses for an event
     */
    public double getTotalExpensesForEvent(int eventId) {
        String sql = "SELECT ISNULL(SUM(TotalCost), 0) FROM EventExpenses WHERE EventID = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException ex) {
            System.err.println("Error summing event expenses: " + ex.getMessage());
            ex.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Sum expenses in a date range
     */
    public double getTotalExpensesInPeriod(LocalDate start, LocalDate end) {
        String sql = "SELECT ISNULL(SUM(TotalCost), 0) FROM EventExpenses WHERE CAST(CreatedAt AS DATE) BETWEEN ? AND ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start.toString());
            stmt.setString(2, end.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException ex) {
            System.err.println("Error summing event expenses in period: " + ex.getMessage());
            ex.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Sum total expenses for a specific event between dates (inclusive)
     */
    public double getTotalExpensesForEventInPeriod(LocalDate start, LocalDate end, int eventId) {
        String sql = "SELECT ISNULL(SUM(TotalCost), 0) FROM EventExpenses WHERE EventID = ? AND CAST(CreatedAt AS DATE) BETWEEN ? AND ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            stmt.setString(2, start.toString());
            stmt.setString(3, end.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException ex) {
            System.err.println("Error summing event expenses for event in period: " + ex.getMessage());
            ex.printStackTrace();
        }
        return 0.0;
    }
}
