package com.scc.dao;

import com.scc.model.Appliance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplianceDao {

    public boolean addAppliance(String brand, String model, String serialNumber, String category, double price, int stockQuantity, String warrantyPeriod) {
        String sql = "INSERT INTO appliances (brand, model, serial_number, category, price, stock_quantity, warranty_period) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, brand.trim());
            ps.setString(2, model.trim());
            ps.setString(3, serialNumber.trim());
            ps.setString(4, category.trim());
            ps.setDouble(5, price);
            ps.setInt(6, stockQuantity);
            ps.setString(7, warrantyPeriod.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to add appliance: " + e.getMessage());
            return false;
        }
    }

    public List<Appliance> getAllAppliances() {
        String sql = "SELECT id, brand, model, serial_number, category, price, stock_quantity, warranty_period FROM appliances ORDER BY id";
        List<Appliance> appliances = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                appliances.add(new Appliance(
                        rs.getInt("id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("serial_number"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getString("warranty_period")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch appliances: " + e.getMessage());
        }
        return appliances;
    }
}