package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public boolean logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        System.out.println(">>> LOG ATTEMPT: userId=" + userId + ", action=" + actionType);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(true);
            ps.setInt(1, userId);
            ps.setString(2, actionType.toUpperCase());
            ps.setString(3, description);

            int rows = ps.executeUpdate();
            System.out.println(">>> LOG SUCCESS! Rows written: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println(">>> LOG FAILED: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getAllLogs() {
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM activity_logs ORDER BY timestamp DESC";
        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                logs.add(mapLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch logs: " + e.getMessage());
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC";
        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch logs for user ID " + userId + ": " + e.getMessage());
        }
        return logs;
    }

    private ActivityLog mapLog(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}