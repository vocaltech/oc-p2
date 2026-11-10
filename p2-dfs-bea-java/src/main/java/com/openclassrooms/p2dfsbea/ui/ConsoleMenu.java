package com.openclassrooms.p2dfsbea.ui;

import com.openclassrooms.p2dfsbea.model.Expense;
import com.openclassrooms.p2dfsbea.repository.ExpenseRepository;
import com.openclassrooms.p2dfsbea.service.CarPurchaseSimulation;
import com.openclassrooms.p2dfsbea.service.ExpenseService;
import com.openclassrooms.p2dfsbea.service.ExpensesStatistics;
import com.openclassrooms.p2dfsbea.service.MortgageSimulation;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class ConsoleMenu {
    // ANSI escape codes
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String UNDERLINE = "\u001B[4m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    private final Scanner scanner = new Scanner(System.in);
    private final ExpenseRepository repository = new ExpenseRepository();
    private final ExpenseService service = new ExpenseService(repository);

    /**
     * Lit un BigDecimal depuis la console.
     *
     * @param label etiquette a afficher
     * @return le BigDecimal lu, ou null si le format est invalide
     */
    private BigDecimal readBigDecimal(String label) {
        System.out.print(YELLOW + label + RESET + " : ");
        String input = scanner.nextLine().trim().replace(',', '.');
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException ex) {
            System.out.println(RED + "Format invalide. Un nombre est attendu." + RESET);
            return null;
        }
    }

    /**
     * Lit une ligne de texte depuis la console.
     *
     * @param label etiquette a afficher
     * @return la valeur lue
     */
    private String readLine(String label) {
        System.out.print(YELLOW + label + RESET + " : ");
        return scanner.nextLine().trim();
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> showExpensesHistory();
                case "2" -> showExpensesStatistics();
                case "3" -> showCarPurchaseSimulator();
                case "4" -> showMortgageSimulator();
                case "5" -> addExpense();
                case "6" -> {
                    System.out.println();
                    System.out.println(CYAN + "╔══════════════════════════════════════════════════╗" + RESET);
                    System.out.println(CYAN + "║" + RESET + BOLD + "   À bientôt pour de nouvelles simulations !      " + RESET + CYAN + "║" + RESET);
                    System.out.println(CYAN + "╚══════════════════════════════════════════════════╝" + RESET);
                    System.out.println();
                    running = false;
                }
                default -> System.out.println(RED + "Option inconnue, veuillez réessayer." + RESET);
            }
        }
    }

    private void showMortgageSimulator() {
        System.out.println();
        System.out.println(CYAN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║" + RESET + BOLD + "         Simulation Prêt Immobilier               " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println();

        //
        // Saisie de l'epargne mensuelle
        //
        BigDecimal monthlySavings = readBigDecimal("Epargne mensuelle (ex: 1000)");
        if (monthlySavings == null) return;
        if (monthlySavings.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println(RED + "L'epargne mensuelle doit être strictement positive." + RESET);
            return;
        }

        //
        // Saisie de la depense mensuelle pour le logement
        //
        BigDecimal monthlyHousingExpense = readBigDecimal("Dépense mensuelle pour le logement (ex: 800)");
        if (monthlyHousingExpense == null) return;
        if (monthlyHousingExpense.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println(RED + "La depense pour le logement ne peut pas être négative." + RESET);
            return;
        }

        //
        // Saisie du ratio d'endettement
        //
        BigDecimal debtToIncomeRatio = readBigDecimal("Ratio d'endettement maximal (ex: 0.35 pour 35%)");
        if (debtToIncomeRatio == null) return;
        if (debtToIncomeRatio.compareTo(BigDecimal.ZERO) < 0
                || debtToIncomeRatio.compareTo(BigDecimal.ONE) > 0) {
            System.out.println(RED + "Le ratio doit être compris entre 0 et 1." + RESET);
            return;
        }

        //
        // Calcul du montant empruntable
        //
        MortgageSimulation mortgageSim;
        try {
            mortgageSim = service.simulateMortgage(monthlySavings, monthlyHousingExpense, debtToIncomeRatio);
        } catch (IllegalStateException ex) {
            System.out.println(RED + ex.getMessage() + RESET);
            return;
        }

        //
        // Affichage des résultats
        //
        System.out.println();
        System.out.println(GREEN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(GREEN + "║" + RESET + BOLD + "           Résultat de la Simulation              " + RESET + GREEN + "║" + RESET);
        System.out.println(GREEN + "╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println();
        System.out.println(WHITE + "  Épargne mensuelle          :" + RESET + " " + GREEN + mortgageSim.monthlySavings() + RESET);
        System.out.println(WHITE + "  Dépense logement          :" + RESET + " " + YELLOW + mortgageSim.monthlyHousingExpense() + RESET);
        System.out.println(WHITE + "  Ratio d'endettement       :" + RESET + " " + CYAN + mortgageSim.debtToIncomeRatio() + RESET);
        System.out.println(WHITE + "  Montant empruntable        :" + RESET + " " + BOLD + GREEN + mortgageSim.borrowableAmount() + RESET);
        System.out.println();
    }

    private void showCarPurchaseSimulator() {
        System.out.println();
        System.out.println(CYAN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║" + RESET + BOLD + "       Simulation Achat d'une voiture             " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println();

        BigDecimal carPrice = readCarPrice();
        if (carPrice == null) {
            return; // input error already reported
        }

        List<Expense> expenses = service.loadAllExpenses();
        if (expenses.isEmpty()) {
            System.out.println(RED + "Aucun historique de depenses disponible. Impossible de calculer l'epargne." + RESET);
            return;
        }

        CarPurchaseSimulation carPurchaseSimulation;
        try {
            carPurchaseSimulation = service.simulateCarPurchase(carPrice, expenses);
        } catch (IllegalStateException ex) {
            System.out.println(RED + ex.getMessage() + RESET);
            return;
        }

        System.out.println();
        System.out.println(GREEN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(GREEN + "║" + RESET + BOLD + "           Résultat de la Simulation              " + RESET + GREEN + "║" + RESET);
        System.out.println(GREEN + "╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println();
        System.out.println(WHITE + "  Prix de la voiture        :" + RESET + " " + CYAN + carPurchaseSimulation.carPrice().setScale(2, RoundingMode.HALF_UP) + RESET);
        System.out.println(WHITE + "  Dépense mensuelle moyenne :" + RESET + " " + YELLOW + carPurchaseSimulation.averageMonthlySpending() + RESET);
        System.out.println(WHITE + "  Épargne mensuelle estimée :" + RESET + " " + GREEN + carPurchaseSimulation.monthlySavings() + RESET);
        System.out.println(WHITE + "  Nombre de mois nécessaire :" + RESET + " " + BOLD + carPurchaseSimulation.monthsRequired() + RESET);
        System.out.println();
    }

    private BigDecimal readCarPrice() {
        BigDecimal price = readBigDecimal("Prix de la voiture (ex: 15000)");
        if (price == null) return null;
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println(RED + "Le prix doit etre strictement positif." + RESET);
            return null;
        }
        return price;
    }

    private void showExpensesStatistics() {
        List<Expense> expenses = service.loadAllExpenses();
        if (expenses.isEmpty()) {
            System.out.println(RED + "Aucun historique de depenses disponible. Impossible de calculer les statistiques." + RESET);
            return;
        }

        ExpensesStatistics expensesStats;
        try {
            expensesStats = service.computeExpensesStatistics(expenses);
        } catch (IllegalStateException ex) {
            System.out.println(RED + ex.getMessage() + RESET);
            return;
        }

        //
        // Total des dépenses
        // sur toute la période
        //
        showExpensesHistory();
        System.out.println();
        System.out.println(CYAN + "─── Total Dépenses (sur toute la période) ───" + RESET);
        System.out.println(BOLD + GREEN + "  *** " + expensesStats.totalExpenses() + " ***" + RESET);

        //
        // Nombre de mois couverts
        //
        System.out.println();
        System.out.println(CYAN + "─── Nombre de mois couverts ───" + RESET);
        System.out.println("  " + expensesStats.monthsCovered().size() + " mois");

        //
        // Total par catégories
        // sur toute la période
        //
        System.out.println();
        System.out.println(CYAN + "─── Total par catégories (sur toute la période) ───" + RESET);
        expensesStats.totalPerCategory().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println(WHITE + "  " + e.getKey() + ":" + RESET + " " + YELLOW + e.getValue().setScale(2, RoundingMode.HALF_UP) + RESET));

        //
        // Total mensuel par catégories
        //
        System.out.println();
        System.out.println(CYAN + "─── Total mensuel par catégories ───" + RESET);

        // Collect all categories for consistent ordering
        Set<String> allCategories = expensesStats.monthlyPerCategory().values().stream()
                .flatMap(m -> m.keySet().stream())
                .collect(Collectors.toSet());
        List<String> sortedCategories = allCategories.stream().sorted().collect(Collectors.toList());

        for (YearMonth ym : expensesStats.monthlyPerCategory().keySet().stream().sorted().collect(Collectors.toList())) {
            String monthLabel = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH)
                    + " " + ym.getYear();
            System.out.println();
            System.out.println(BOLD + WHITE + "  " + monthLabel + ":" + RESET);
            Map<String, BigDecimal> catMap = expensesStats.monthlyPerCategory().get(ym);
            for (String cat : sortedCategories) {
                BigDecimal total = catMap.getOrDefault(cat, BigDecimal.ZERO);
                System.out.println("      " + cat + ": " + total.setScale(2, RoundingMode.HALF_UP));
            }
        }

        //
        // Moyennes mensuelles
        //
        System.out.println();
        System.out.println(CYAN + "─── Moyennes mensuelles ───" + RESET);
        expensesStats.averagePerCategory().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(ent -> System.out.println(WHITE + "  " + ent.getKey() + ":" + RESET + " " + GREEN + ent.getValue() + RESET));

        System.out.println();
        System.out.println(WHITE + "  Total mensuel moyen:" + RESET + " " + BOLD + expensesStats.monthlyAverageTotal() + RESET);
    }

    private void printMenu() {
        System.out.println();
        System.out.println(CYAN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║" + RESET + BOLD + "        Personal Finance Tracker                  " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println();
        System.out.println(BLUE + "  1" + RESET + ") Show history");
        System.out.println(BLUE + "  2" + RESET + ") Statistiques");
        System.out.println(BLUE + "  3" + RESET + ") Simulation achat d'une voiture");
        System.out.println(BLUE + "  4" + RESET + ") Simulation prêt immobilier");
        System.out.println(BLUE + "  5" + RESET + ") Add expense");
        System.out.println(BLUE + "  6" + RESET + ") Quit");
        System.out.println();
        System.out.print(CYAN + "Choose an option: " + RESET);
    }

    private void showExpensesHistory() {
        List<Expense> list = service.loadAllExpenses();
        System.out.println();
        System.out.println(CYAN + "─── Historique des dépenses (le plus récent en premier) ───" + RESET);
        if (list.isEmpty()) {
            System.out.println(RED + "  Aucune dépense enregistrée." + RESET);
        } else {
            list.stream().sorted((a, b) -> b.getDate().compareTo(a.getDate())).forEach(e -> System.out.println(e.toCsvLine()));
        }
    }

    private void addExpense() {
        System.out.println();
        System.out.println(CYAN + "─── Ajouter une dépense ───" + RESET);
        System.out.println();

        String dateS = readLine("Date (dd/MM/yyyy)");
        LocalDate date;
        try {
            date = LocalDate.parse(dateS, Expense.FORMATTER);
        } catch (DateTimeParseException dtpe) {
            System.out.println(RED + "Format de date invalide. Utilisez dd/MM/yyyy." + RESET);
            return;
        }

        String category = readLine("Catégorie");
        if (category == null || category.isEmpty()) {
            System.out.println(RED + "La catégorie ne peut pas être vide." + RESET);
            return;
        }

        String description = readLine("Description");
        if (description == null || description.isEmpty()) {
            System.out.println(RED + "La description ne peut pas être vide." + RESET);
            return;
        }

        BigDecimal amount = readBigDecimal("Montant (ex: 12.50)");
        if (amount == null) return;

        try {
            Expense e = new Expense(date, category, description, amount);
            service.addExpense(e);
            System.out.println();
            System.out.println(GREEN + "Dépense ajoutée avec succès." + RESET);
        } catch (IOException io) {
            System.out.println(RED + "Impossible de sauvegarder la dépense: " + io.getMessage() + RESET);
        } catch (Exception ex) {
            System.out.println(RED + "Erreur: " + ex.getMessage() + RESET);
        }
    }
}
