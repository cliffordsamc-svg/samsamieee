package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.ApplianceDao;
import com.scc.model.Appliance;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class SessionMenu {

    private final Scanner scanner;
    private final User user;
    private final ActivityLogDao logDao;
    private final ApplianceDao applianceDao;

    public SessionMenu(Scanner scanner, User user) {
        this.scanner = scanner;
        this.user = user;
        this.logDao = new ActivityLogDao();
        this.applianceDao = new ApplianceDao();
    }

    public void run() {
        System.out.println("\nWelcome, " + user.username()
                + " | Role: " + user.roles()
                + " | Status: " + user.status());

        while (true) {
            System.out.println("\n===== Dashboard (" + user.roles() + ") =====");
            System.out.println("1. Add New Appliance (CREATE)");
            System.out.println("2. View All Appliances");
            System.out.println("3. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addAppliance();
                case "2" -> viewAppliances();
                case "3" -> {
                    System.out.println("Logged out successfully.");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void addAppliance() {
        System.out.println("\n--- Add New Appliance Item ---");

        System.out.print("Enter Brand: ");
        String brand = scanner.nextLine().trim();

        System.out.print("Enter Model: ");
        String model = scanner.nextLine().trim();

        System.out.print("Enter Serial Number: ");
        String serialNumber = scanner.nextLine().trim();

        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();

        double price = readDoubleInput("Enter Price: ");
        int stock = readIntInput("Enter Stock Quantity: ");

        System.out.print("Enter Warranty Period (e.g., 1 Year): ");
        String warranty = scanner.nextLine().trim();

        if (brand.isEmpty() || model.isEmpty() || serialNumber.isEmpty()) {
            System.out.println("Required fields cannot be empty.");
            return;
        }

        boolean success = applianceDao.addAppliance(brand, model, serialNumber, category, price, stock, warranty);
        if (success) {
            System.out.println("Success: Appliance record added successfully!");
            // Log action to the enterprise activity log audit trail
            logDao.logAction(user.id(), "APPLIANCE_CREATED", "User " + user.username() + " added appliance: " + brand + " " + model);
        } else {
            System.out.println("Failed to add appliance record.");
        }
    }

    private void viewAppliances() {
        List<Appliance> appliances = applianceDao.getAllAppliances();
        if (appliances.isEmpty()) {
            System.out.println("No appliances found in inventory.");
            return;
        }

        System.out.println("\n--- Appliance Catalog ---");
        for (Appliance app : appliances) {
            System.out.printf("ID: %d | Brand: %s | Model: %s | Category: %s | Price: ₱%.2f | Stock: %d | Warranty: %s\n",
                    app.id(), app.brand(), app.model(), app.category(), app.price(), app.stockQuantity(), app.warrantyPeriod());
        }
    }

    private double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }
}