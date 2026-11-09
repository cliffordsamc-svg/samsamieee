package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    private final Scanner scanner;
    private final UserDao userDao = new UserDao();

    public Authentication(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Main menu loop: Register, Login, Exit. */
    public void run() {
        while (true) {
            System.out.println("\n===== Clifford Appliance Shop =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- Register Account ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.println("Role: 1) ADMIN  2) STOCK_MANAGER  3) CASHIER");
        System.out.print("Choose: ");
        String role = switch (scanner.nextLine().trim()) {
            case "1" -> "ADMIN";
            case "2" -> "STOCK_MANAGER";
            case "3" -> "CASHIER";
            default -> null;
        };

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password are required.");
            return;
        }
        if (role == null) {
            System.out.println("Invalid role.");
            return;
        }

        try {
            if (userDao.usernameExists(username)) {
                System.out.println("Username already taken.");
                return;
            }
            if (userDao.register(username, password, role)) {
                System.out.println("Registered as " + role + ". Account status: PENDING.");
            }
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void login() {
        System.out.println("\n--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            User user = userDao.login(username, password);
            if (user == null) {
                System.out.println("Invalid username or password.");
                return;
            }
            if (!"APPROVED".equalsIgnoreCase(user.status())) {
                System.out.println("Your account is " + user.status() + ". Please wait for admin approval.");
                return;
            }
            routeByRole(user);
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    /** Sends each role to its own menu. */
    private void routeByRole(User user) {
        switch (user.roles()) {
            case "ADMIN" -> new AdminMenu(scanner, userDao, user).run();
            case "STOCK_MANAGER", "CASHIER" -> staffMenu(user);
            default -> System.out.println("Unknown role: " + user.roles());
        }
    }

    /** Placeholder menu for Stock Manager and Cashier until their features are built. */
    private void staffMenu(User user) {
        System.out.println("\nWelcome, " + user.username() + " | Role: " + user.roles());

        while (true) {
            System.out.println("\n1. Logout");
            System.out.print("Choose: ");
            if (scanner.nextLine().trim().equals("1")) {
                System.out.println("Logged out successfully.");
                return;
            }
            System.out.println("Invalid choice.");
        }
    }
}