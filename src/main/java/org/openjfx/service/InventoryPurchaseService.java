package org.openjfx.service;

import org.openjfx.model.InventoryPurchase;
import org.openjfx.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing inventory purchase records and expense aggregations
 */
public class InventoryPurchaseService {

    public InventoryPurchaseService() {}

    public boolean addPurchase(InventoryPurchase p) {
        String sql = "INSERT INTO InventoryPurchases (ItemID, EventID, Quantity, UnitCost, TotalCost, PurchaseDate, Supplier, Notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (p.getItemId() != null) stmt.setInt(1, p.getItemId()); else stmt.setNull(1, java.sql.Types.INTEGER);
            if (p.getEventId() != null) stmt.setInt(2, p.getEventId()); else stmt.setNull(2, java.sql.Types.INTEGER);
            stmt.setInt(3, p.getQuantity());
            stmt.setDouble(4, p.getUnitCost());
            stmt.setDouble(5, p.getTotalCost());
            Timestamp ts = p.getPurchaseDate() != null ? Timestamp.valueOf(p.getPurchaseDate()) : Timestamp.valueOf(LocalDateTime.now());
            stmt.setTimestamp(6, ts);
            stmt.setString(7, p.getSupplier());
            stmt.setString(8, p.getNotes());

            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting inventory purchase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<InventoryPurchase> getPurchasesInPeriod(LocalDate start, LocalDate end) {
        List<InventoryPurchase> list = new ArrayList<>();
        String sql = "SELECT * FROM InventoryPurchases WHERE PurchaseDate BETWEEN ? AND ? ORDER BY PurchaseDate DESC";
        Timestamp tsStart = Timestamp.valueOf(LocalDateTime.of(start, LocalTime.MIN));
        Timestamp tsEnd = Timestamp.valueOf(LocalDateTime.of(end, LocalTime.MAX));

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, tsStart);
            stmt.setTimestamp(2, tsEnd);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InventoryPurchase p = mapRow(rs);
                    list.add(p);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching purchases in period: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public double getTotalExpensesInPeriod(LocalDate start, LocalDate end) {
        String sql = "SELECT ISNULL(SUM(TotalCost),0) as Total FROM InventoryPurchases WHERE PurchaseDate BETWEEN ? AND ?";
        Timestamp tsStart = Timestamp.valueOf(LocalDateTime.of(start, LocalTime.MIN));
        Timestamp tsEnd = Timestamp.valueOf(LocalDateTime.of(end, LocalTime.MAX));

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, tsStart);
            stmt.setTimestamp(2, tsEnd);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("Total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting total expenses in period: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getTotalExpensesForEvent(int eventId) {
        String sql = "SELECT ISNULL(SUM(TotalCost),0) as Total FROM InventoryPurchases WHERE EventID = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("Total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting total expenses for event: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    private InventoryPurchase mapRow(ResultSet rs) throws SQLException {
        InventoryPurchase p = new InventoryPurchase();
        p.setPurchaseId(rs.getInt("PurchaseID"));
        Integer itemId = rs.getObject("ItemID", Integer.class);
        p.setItemId(itemId);
        Integer eventId = rs.getObject("EventID", Integer.class);
        p.setEventId(eventId);
        p.setQuantity(rs.getInt("Quantity"));
        p.setUnitCost(rs.getDouble("UnitCost"));
        p.setTotalCost(rs.getDouble("TotalCost"));
        Timestamp ts = rs.getTimestamp("PurchaseDate");
        if (ts != null) p.setPurchaseDate(ts.toLocalDateTime());
        p.setSupplier(rs.getString("Supplier"));
        p.setNotes(rs.getString("Notes"));
        return p;
    }
}
