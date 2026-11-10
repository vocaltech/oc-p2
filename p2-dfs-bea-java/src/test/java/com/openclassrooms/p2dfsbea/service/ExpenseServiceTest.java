package com.openclassrooms.p2dfsbea.service;

import com.openclassrooms.p2dfsbea.model.Expense;
import com.openclassrooms.p2dfsbea.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpenseServiceTest {
    private ExpenseService service;

    private final Expense e1 = new Expense(LocalDate.of(2024, 1, 15), "Alimentation", "Courses", new BigDecimal("100.00"));
    private final Expense e2 = new Expense(LocalDate.of(2024, 1, 20), "Logement", "Loyer", new BigDecimal("800.00"));
    private final Expense e3 = new Expense(LocalDate.of(2024, 2, 10), "Alimentation", "Restaurant", new BigDecimal("50.00"));
    private final Expense e4 = new Expense(LocalDate.of(2024, 2, 20), "Transport", "Essence", new BigDecimal("60.00"));
    private final Expense e5 = new Expense(LocalDate.of(2024, 3, 5), "Loisirs", "Cinema", new BigDecimal("30.00"));

    @BeforeEach
    void setUp() {
        ExpenseRepository repository = new ExpenseRepository();
        service = new ExpenseService(repository);
    }

    @Test
    @DisplayName("getTotalExpenses returns the sum of all amounts")
    void givenExpensesList_whenGetTotalExpenses_thenReturnSumOfAllAmounts() {
        // Given
        List<Expense> expenses = Arrays.asList(e1, e2, e3, e4, e5);

        // When
        BigDecimal total = service.getTotalExpenses(expenses);

        // Then
        assertThat(total).isEqualByComparingTo(new BigDecimal("1040.00"));
    }

    @Test
    @DisplayName("getTotalExpenses returns zero for empty list")
    void givenEmptyExpensesList_whenGetTotalExpenses_thenReturnZero() {
        BigDecimal total = service.getTotalExpenses(List.of());

        assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("getMonthsCovered returns unique months")
    void givenFiveExpenses_whenGetMonthsCovered_thenReturnUniqueMonths() {
        List<Expense> expenses = Arrays.asList(e1, e2, e3, e4, e5);

        Set<YearMonth> months = service.getMonthsCovered(expenses);

        assertThat(months).containsExactlyInAnyOrder(
            YearMonth.of(2024, 1),
            YearMonth.of(2024, 2),
            YearMonth.of(2024, 3)
        );
    }

    @Test
    @DisplayName("simulateCarPurchase computes correct months required")
    void givenFiveExpenses_whenSimulateCarPurchase_thenReturnCorrectMonthsRequired() {
        List<Expense> expenses = Arrays.asList(e1, e2, e3, e4, e5);

        CarPurchaseSimulation sim = service.simulateCarPurchase(new BigDecimal("5000"), expenses);

        assertThat(sim.carPrice()).isEqualByComparingTo("5000.00");
        assertThat(sim.monthlySavings()).isEqualByComparingTo("346.67");
        assertThat(sim.monthsRequired()).isEqualByComparingTo("15");
    }

    @Test
    @DisplayName("simulateCarPurchase throws when expenses empty")
    void givenEmptyExpensesList_whenSimulateCarPurchase_thenThrowIllegalStateException() {
        assertThatThrownBy(() -> service.simulateCarPurchase(new BigDecimal("5000"), List.of()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Aucune donnee de depenses disponible. Impossible de calculer l'epargne.");
    }

    @Test
    @DisplayName("simulateMortgage computes borrowable amount correctly")
    void givenValidParameters_whenSimulateMortgage_thenReturnCorrectBorrowableAmount() {
        MortgageSimulation sim = service.simulateMortgage(
            new BigDecimal("1500"),
            new BigDecimal("800"),
            new BigDecimal("0.35")
        );

        assertThat(sim.monthlySavings()).isEqualByComparingTo("1500.00");
        assertThat(sim.monthlyHousingExpense()).isEqualByComparingTo("800.00");
        assertThat(sim.debtToIncomeRatio()).isEqualByComparingTo("0.35");
        assertThat(sim.borrowableAmount()).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("simulateMortgage throws when monthlySavings is null")
    void givenNullMonthlySavings_whenSimulateMortgage_thenThrowIllegalStateException() {
        assertThatThrownBy(() -> service.simulateMortgage(null, new BigDecimal("800"), new BigDecimal("0.35")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("L'epargne mensuelle doit être strictement positive.");
    }

    @Test
    @DisplayName("simulateMortgage throws when monthlySavings is zero")
    void givenZeroMonthlySavings_whenSimulateMortgage_thenThrowIllegalStateException() {
        assertThatThrownBy(() -> service.simulateMortgage(BigDecimal.ZERO, new BigDecimal("800"), new BigDecimal("0.35")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("L'epargne mensuelle doit être strictement positive.");
    }

    @Test
    @DisplayName("simulateMortgage throws when housingExpense exceeds savings")
    void givenHousingExpenseExceedsSavings_whenSimulateMortgage_thenThrowIllegalStateException() {
        assertThatThrownBy(() -> service.simulateMortgage(new BigDecimal("500"), new BigDecimal("800"), new BigDecimal("0.35")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("La depense mensuelle pour le logement dépasse l'epargne mensuelle disponible.");
    }

    @Test
    @DisplayName("simulateMortgage throws when debtToIncomeRatio is out of range")
    void givenOutOfRangeDebtToIncomeRatio_whenSimulateMortgage_thenThrowIllegalStateException() {
        assertThatThrownBy(() -> service.simulateMortgage(new BigDecimal("1500"), new BigDecimal("800"), new BigDecimal("1.5")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Le ratio d'endettement doit être compris entre 0 et 1.");

        assertThatThrownBy(() -> service.simulateMortgage(new BigDecimal("1500"), new BigDecimal("800"), new BigDecimal("-0.1")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Le ratio d'endettement doit être compris entre 0 et 1.");
    }

    @Test
    @DisplayName("computeExpensesStatistics computes all indicators")
    void givenFiveExpenses_whenComputeExpensesStatistics_thenReturnAllIndicators() {
        List<Expense> expenses = Arrays.asList(e1, e2, e3, e4, e5);

        ExpensesStatistics stats = service.computeExpensesStatistics(expenses);

        assertThat(stats.totalExpenses()).isEqualByComparingTo("1040.00");
        assertThat(stats.monthsCovered()).hasSize(3);
        assertThat(stats.monthlyAverageTotal()).isEqualByComparingTo("346.67");
        assertThat(stats.totalPerCategory()).containsEntry("Alimentation", new BigDecimal("150.00"));
        assertThat(stats.totalPerCategory()).containsEntry("Logement", new BigDecimal("800.00"));
        assertThat(stats.averagePerCategory()).containsEntry("Alimentation", new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("computeExpensesStatistics throws when no expenses")
    void givenEmptyExpensesList_whenComputeExpensesStatistics_thenThrowIllegalStateException() {
        assertThatThrownBy(() -> service.computeExpensesStatistics(List.of()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Aucune donnee de depenses disponible. Impossible de calculer les statistiques.");
    }
}
