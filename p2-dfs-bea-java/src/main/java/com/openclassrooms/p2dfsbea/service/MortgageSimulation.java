package com.openclassrooms.p2dfsbea.service;

import java.math.BigDecimal;

public record MortgageSimulation(
        BigDecimal monthlySavings,
        BigDecimal monthlyHousingExpense,
        BigDecimal debtToIncomeRatio,
        BigDecimal borrowableAmount
) {
}