package com.openclassrooms.p2dfsbea.service;

import java.math.BigDecimal;

public record CarPurchaseSimulation(
        BigDecimal carPrice,
        BigDecimal averageMonthlySpending,
        BigDecimal monthlySavings,
        BigDecimal monthsRequired
) {
}