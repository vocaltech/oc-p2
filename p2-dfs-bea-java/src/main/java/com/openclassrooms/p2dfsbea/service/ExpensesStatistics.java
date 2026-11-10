package com.openclassrooms.p2dfsbea.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;

public record ExpensesStatistics(
        BigDecimal totalExpenses,
        Set<YearMonth> monthsCovered,
        Map<String, BigDecimal> totalPerCategory,
        Map<YearMonth, Map<String, BigDecimal>> monthlyPerCategory,
        Map<String, BigDecimal> averagePerCategory,
        BigDecimal monthlyAverageTotal
) {
}