package com.openclassrooms.p2dfsbea.service;

import com.openclassrooms.p2dfsbea.model.Expense;
import com.openclassrooms.p2dfsbea.repository.ExpenseRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpenseService {
    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public List<Expense> loadAllExpenses() {
        return repository.readAll();
    }

    public void addExpense(Expense expense) throws IOException {
        repository.append(expense);
    }

    public BigDecimal getTotalExpenses(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Set<YearMonth> getMonthsCovered(List<Expense> expenses) {
        return expenses.stream()
                .map(e -> YearMonth.from(e.getDate()))
                .collect(Collectors.toSet());
    }

    /**
     * Simule le nombre de mois necessaires pour epargner le prix d'une voiture.
     * <p>
     * Hypothese : l'utilisateur peut epargner un montant equivalent a sa depense
     * mensuelle moyenne.
     *
     * @param carPrice prix de la voiture (doit etre strictement positif)
     * @param expenses liste des depenses historiques
     * @return le detail de la simulation
     * @throws IllegalStateException si aucun historique de depenses n'est disponible
     *                              ou si la depense mensuelle moyenne est nulle
     */
    public CarPurchaseSimulation simulateCarPurchase(BigDecimal carPrice, List<Expense> expenses) {
        BigDecimal totalExpenses = getTotalExpenses(expenses);
        Set<YearMonth> monthsCovered = getMonthsCovered(expenses);

        if (monthsCovered.isEmpty()) {
            throw new IllegalStateException(
                    "Aucune donnee de depenses disponible. Impossible de calculer l'epargne.");
        }

        BigDecimal averageMonthlySpending = totalExpenses
                .divide(BigDecimal.valueOf(monthsCovered.size()), 2, RoundingMode.HALF_UP);

        if (averageMonthlySpending.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Impossible de calculer l'epargne. La depense mensuelle moyenne est nulle.");
        }

        BigDecimal monthlySavings = averageMonthlySpending;
        BigDecimal monthsRequired = carPrice.divide(monthlySavings, 0, RoundingMode.UP);

        return new CarPurchaseSimulation(
                carPrice, averageMonthlySpending, monthlySavings, monthsRequired);
    }

    /**
     * Calcule le montant empruntable pour un achat immobilier.
     * <p>
     * Le montant empruntable est determine a partir de l'epargne mensuelle
     * disponible, de la depense mensuelle pour le logement et du ratio
     * d'endettement maximal (35 % par defaut).
     * <p>
     * Formule : montant empruntable = (epargne mensuelle - depense logement) / ratio
     *
     * @param monthlySavings epargne mensuelle disponible
     * @param monthlyHousingExpense depense mensuelle pour le logement
     * @param debtToIncomeRatio ratio d'endettement maximal (doit etre entre 0 et 1)
     * @return le detail de la simulation
     * @throws IllegalStateException si l'epargne est nulle ou negative, si la depense
     *                              logement est negative, ou si le ratio est hors de
     *                              la plage [0 ; 1]
     */
    public MortgageSimulation simulateMortgage(BigDecimal monthlySavings, BigDecimal monthlyHousingExpense, BigDecimal debtToIncomeRatio) {
        if (monthlySavings == null || monthlySavings.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "L'epargne mensuelle doit être strictement positive.");
        }

        if (monthlyHousingExpense == null || monthlyHousingExpense.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "La depense mensuelle pour le logement ne peut pas être négative.");
        }

        if (monthlyHousingExpense.compareTo(monthlySavings) > 0) {
            throw new IllegalStateException(
                    "La depense mensuelle pour le logement dépasse l'epargne mensuelle disponible.");
        }

        if (debtToIncomeRatio == null
                || debtToIncomeRatio.compareTo(BigDecimal.ZERO) < 0
                || debtToIncomeRatio.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalStateException(
                    "Le ratio d'endettement doit être compris entre 0 et 1.");
        }

        BigDecimal availableForMortgage = monthlySavings.subtract(monthlyHousingExpense);
        BigDecimal borrowableAmount = availableForMortgage.divide(debtToIncomeRatio, 2, RoundingMode.HALF_UP);

        return new MortgageSimulation(monthlySavings, monthlyHousingExpense, debtToIncomeRatio, borrowableAmount);
    }

    /**
     * Calcule l'ensemble des indicateurs statistiques sur une liste de depenses.
     *
     * @param expenses liste des depenses historiques
     * @return le detail des statistiques (total, mois couverts, totaux par
     *         categorie, totaux mensuels par categorie, moyennes par categorie
     *         et total mensuel moyen)
     * @throws IllegalStateException si aucun mois n'est couvert (division par zero
     *                              lors du calcul des moyennes)
     */
    public ExpensesStatistics computeExpensesStatistics(List<Expense> expenses) {
        BigDecimal totalExpenses = getTotalExpenses(expenses);
        Set<YearMonth> monthsCovered = getMonthsCovered(expenses);

        if (monthsCovered.isEmpty()) {
            throw new IllegalStateException(
                    "Aucune donnee de depenses disponible. Impossible de calculer les statistiques.");
        }

        Map<String, BigDecimal> totalPerCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        Map<YearMonth, Map<String, BigDecimal>> monthlyPerCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> YearMonth.from(e.getDate()),
                        Collectors.groupingBy(
                                Expense::getCategory,
                                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add))));

        Map<String, BigDecimal> averagePerCategory = totalPerCategory.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()
                                .divide(BigDecimal.valueOf(monthsCovered.size()), 2, RoundingMode.HALF_UP)));

        BigDecimal monthlyAverageTotal = totalExpenses
                .divide(BigDecimal.valueOf(monthsCovered.size()), 2, RoundingMode.HALF_UP);

        return new ExpensesStatistics(
                totalExpenses,
                monthsCovered,
                totalPerCategory,
                monthlyPerCategory,
                averagePerCategory,
                monthlyAverageTotal);
    }
}
