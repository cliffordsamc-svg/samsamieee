package com.scc.model;

public record Appliance(
        int id,
        String brand,
        String model,
        String serialNumber,
        String category,
        double price,
        int stockQuantity,
        String warrantyPeriod
) {}