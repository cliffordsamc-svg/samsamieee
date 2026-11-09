package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final User admin;

    public AdminMenu(Scanner scanner, UserDao userDao, User admin) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.logDao = new ActivityLogDao();
        this.admin = admin;
    }

    public void run() {
        System.out.println("\nWelcome, " + admin.username() + " (ADMIN)");

        while (true) {
            System.out.println("\n===== Admin Menu =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role & Status");
            System.out.println("5. Delete User Account");
            System.out.println("6. View System Audit Logs");
            System.out.println("7. Logout");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> updateUserRoleAndStatus();
                case "5" -> deleteUser();
                case "6" -> viewAuditLogsMenu();
                case "7" -> {
                    System.out.println("Logged out successfully.");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewAllUsers() {
        try {
            System.out.println("\n--- All System Users ---");
            printUsers(userDao.getAllUsers());
        } catch (SQLException e) {
            System.out.println("Could not load users: " + e.getMessage());
        }
    }

    private void viewPendingUsers() {
        try {
            System.out.println("\n--- Pending Registrations ---");
            printUsers(userDao.getPendingUsers());
        } catch (SQLException e) {
            System.out.println("Could not load pending users: " + e.getMessage());
        }
    }

    private void approveUser() {
        List<User> pending;
        try {
            pending = userDao.getPendingUsers();
        } catch (SQLException e) {
            System.out.println("Could not load pending users: " + e.getMessage());
            return;
        }

        System.out.println("\n--- Approve User Account ---");
        if (pending.isEmpty()) {
            System.out.println("No pending registrations.");
            return;
        }
        printUsers(pending);

        while (true) {
            int targetId = readIntInput("Enter Target User ID to approve (0 to cancel): ");

            if (targetId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }
            if (!isInList(pending, targetId)) {
                System.out.println("ID " + targetId + " is not in the pending list. Try again.");
                continue;
            }

            try {
                if (userDao.approveUser(targetId)) {
                    System.out.println("User ID " + targetId + " has been APPROVED.");
                    // Auto-log account approval
                    logDao.logAction(admin.id(), "USER_APPROVED", "Admin ID " + admin.id() + " approved account for User ID " + targetId);
                } else {
                    System.out.println("Approval failed. No account was updated.");
                }
            } catch (SQLException e) {
                System.out.println("Approval failed: " + e.getMessage());
            }
            return;
        }
    }

    private void updateUserRoleAndStatus() {
        List<User> users;
        try {
            users = userDao.getAllUsers();
        } catch (SQLException e) {
            System.out.println("Could not load users: " + e.getMessage());
            return;
        }

        System.out.println("\n--- Update User Role & Status ---");
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        printUsers(users);

        int targetId = readIntInput("Enter Target User ID to Update (0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        if (targetId == admin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        if (!isInList(users, targetId)) {
            System.out.println("Error: User ID " + targetId + " not found.");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) return;

        String newStatus = readValidStatus();
        if (newStatus == null) return;

        try {
            boolean success = userDao.updateUserRoleAndStatus(targetId, newRole, newStatus);
            if (success) {
                System.out.println("Success: User ID " + targetId + " updated successfully!");
                // Auto-log role/status update
                logDao.logAction(admin.id(), "USER_UPDATED", "Admin ID " + admin.id() + " changed role of User ID " + targetId + " to " + newRole + " and status to " + newStatus);
            } else {
                System.out.println("Update failed. No account was modified.");
            }
        } catch (SQLException e) {
            System.out.println("Database error during update: " + e.getMessage());
        }
    }

    private void deleteUser() {
        List<User> users;
        try {
            users = userDao.getAllUsers();
        } catch (SQLException e) {
            System.out.println("Could not load users: " + e.getMessage());
            return;
        }

        System.out.println("\n--- Delete User Account ---");
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        printUsers(users);

        int targetId = readIntInput("Enter Target User ID to Delete (0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (targetId == admin.id()) {
            System.out.println("WARNING: Deleting the currently active admin account is restricted.");
            return;
        }

        if (!isInList(users, targetId)) {
            System.out.println("Error: User ID " + targetId + " not found.");
            return;
        }

        System.out.print("Are you sure you want to delete User ID " + targetId + "? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (!confirmation.equals("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        try {
            boolean success = userDao.deleteUser(targetId);
            if (success) {
                System.out.println("Success: User ID " + targetId + " has been deleted.");
                // Auto-log user deletion
                logDao.logAction(admin.id(), "USER_DELETED", "Admin ID " + admin.id() + " deleted User ID " + targetId);
            } else {
                System.out.println("Deletion failed. No account was removed.");
            }
        } catch (SQLException e) {
            System.out.println("Database error during deletion: " + e.getMessage());
        }
    }

    private void viewAuditLogsMenu() {
        while (true) {
            System.out.println("\n===== System Audit Logs =====");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Menu");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    List<ActivityLog> logs = logDao.getAllLogs();
                    printAuditLogs(logs);
                }
                case "2" -> {
                    int userId = readIntInput("Enter User ID to filter: ");
                    List<ActivityLog> userLogs = logDao.getLogsByUserId(userId);
                    printAuditLogs(userLogs);
                }
                case "3" -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numeric integer.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new Role (ADMIN, STOCK_MANAGER, CASHIER) or 0 to cancel: ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0")) {
                System.out.println("Update cancelled.");
                return null;
            }

            if (role.equals("ADMIN") || role.equals("STOCK_MANAGER") || role.equals("CASHIER")) {
                return role;
            }
            System.out.println("Invalid role. Only ADMIN, STOCK_MANAGER, or CASHIER are accepted. Try again.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new Status (APPROVED, PENDING) or 0 to cancel: ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0")) {
                System.out.println("Update cancelled.");
                return null;
            }

            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status. Only APPROVED or PENDING are accepted. Try again.");
        }
    }

    private boolean isInList(List<User> users, int id) {
        for (User user : users) {
            if (user.id() == id) {
                return true;
            }
        }
        return false;
    }

    private void printUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        String border = "+" + "-".repeat(7) + "+" + "-".repeat(17) + "+" + "-".repeat(17) + "+" + "-".repeat(12) + "+";
        String rowFormat = "| %-5s | %-15s | %-15s | %-10s |%n";

        System.out.println(border);
        System.out.printf(rowFormat, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);
        for (User user : users) {
            System.out.printf(rowFormat, user.id(), user.username(), user.roles(), user.status());
        }
        System.out.println(border);
        System.out.println("Total: " + users.size());
    }

    private void printAuditLogs(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        String border = "+" + "-".repeat(5) + "+" + "-".repeat(8) + "+" + "-".repeat(18) + "+" + "-".repeat(35) + "+" + "-".repeat(20) + "+";
        String rowFormat = "| %-3s | %-6s | %-16s | %-33s | %-18s |%n";

        System.out.println(border);
        System.out.printf(rowFormat, "ID", "UID", "ACTION TYPE", "DESCRIPTION", "TIMESTAMP");
        System.out.println(border);
        for (ActivityLog log : logs) {
            System.out.printf(rowFormat, log.id(), log.userId(), log.actionType(), log.description(), log.timestamp());
        }
        System.out.println(border);
        System.out.println("Total Logs: " + logs.size());
    }
}