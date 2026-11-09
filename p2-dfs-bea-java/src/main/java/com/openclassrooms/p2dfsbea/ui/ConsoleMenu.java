package com.openclassrooms.p2dfsbea.ui;

import com.openclassrooms.p2dfsbea.model.Expense;
import com.openclassrooms.p2dfsbea.repository.ExpenseRepository;
import com.openclassrooms.p2dfsbea.service.ExpenseService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class ConsoleMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final ExpenseRepository repository = new ExpenseRepository();
    private final ExpenseService service = new ExpenseService(repository);

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> showHistory();
                case "2" -> showStatistics();
                case "3" -> addExpense();
                case "4" -> {
                    System.out.println("Goodbye");
                    running = false;
                }
                default -> System.out.println("Unknown option, please try again.");
            }
        }
    }

    private void showStatistics() {
        List<Expense> expenses = service.loadAllExpenses();

        //
        // Total des dépenses
        // sur toute la période
        //
        System.out.println("\nHistory (most recent first):");
        BigDecimal totalExpenses = computeTotalExpenses(expenses);
        System.out.println("\n --- Total Dépenses (sur toute la période) ---");
        System.out.println("\n *** " + totalExpenses + " ***");

        //
        // Nombre de mois couverts
        //
        Set<YearMonth> monthsCovered = expenses.stream()
                .map(e -> YearMonth.from(e.getDate()))
                .collect(Collectors.toSet());
        System.out.println("\n --- Nombre de mois couverts ---");
        System.out.println("  " + monthsCovered.size());

        //
        // Total par catégories
        // sur toute la période
        //
        System.out.println("\n --- Total par catégories (sur toute la période) ---");

        Map<String, BigDecimal> mapExpensesPerCategorie = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        mapExpensesPerCategorie.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue().setScale(2, RoundingMode.HALF_UP)));

        //
        // Total mensuel par catégories
        //
        System.out.println("\n --- Total mensuel par catégories ---");

        // Build a nested map: YearMonth -> Category -> Total
        Map<YearMonth, Map<String, BigDecimal>> monthlyPerCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> YearMonth.from(e.getDate()),
                        Collectors.groupingBy(
                                Expense::getCategory,
                                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add))));

        // Collect all categories for consistent ordering
        Set<String> allCategories = monthlyPerCategory.values().stream()
                .flatMap(m -> m.keySet().stream())
                .collect(Collectors.toSet());
        List<String> sortedCategories = allCategories.stream().sorted().collect(Collectors.toList());

        for (YearMonth ym : monthlyPerCategory.keySet().stream().sorted().collect(Collectors.toList())) {
            String monthLabel = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH)
                    + " " + ym.getYear();
            System.out.println("  " + monthLabel + ":");
            Map<String, BigDecimal> catMap = monthlyPerCategory.get(ym);
            for (String cat : sortedCategories) {
                BigDecimal total = catMap.getOrDefault(cat, BigDecimal.ZERO);
                System.out.println("    " + cat + ": " + total.setScale(2, RoundingMode.HALF_UP));
            }
        }

        //
        // Moyennes mensuelles
        //
        System.out.println("\n --- Moyennes mensuelles ---");

        Map<String, BigDecimal> avgPerCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().divide(BigDecimal.valueOf(monthsCovered.size()), 2, RoundingMode.HALF_UP)));

        avgPerCategory.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(ent -> System.out.println("  " + ent.getKey() + ": " + ent.getValue()));

        BigDecimal monthlyAvg = totalExpenses
                .divide(BigDecimal.valueOf(monthsCovered.size()), 2, RoundingMode.HALF_UP);
        System.out.println("  Total mensuel moyen: " + monthlyAvg);
    }

    private BigDecimal computeTotalExpenses(List<Expense> expenses) {
        return expenses.stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .peek(e -> System.out.println(e.toCsvLine()))
                .map(e -> e.getAmount())
                .reduce(BigDecimal.ZERO, (acc, amount) -> acc.add(amount));
    }

    private void printMenu() {
        System.out.println("\n--- Personal Finance Tracker ---");
        System.out.println("1) Show history");
        System.out.println("2) Statistiques");
        System.out.println("3) Add expense");
        System.out.println("4) Quit");
        System.out.print("Choose an option: ");
    }

    private void showHistory() {
        List<Expense> list = service.loadAllExpenses();
        System.out.println("\nHistory (most recent first):");
        list.stream().sorted((a, b) -> b.getDate().compareTo(a.getDate())).forEach(e -> System.out.println(e.toCsvLine()));
    }

    private void addExpense() {
        try {
            System.out.print("Date (dd/MM/yyyy): ");
            String dateS = scanner.nextLine().trim();
            LocalDate date = LocalDate.parse(dateS, Expense.FORMATTER);

            System.out.print("Category: ");
            String category = scanner.nextLine().trim();

            System.out.print("Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Amount (e.g. 12.50): ");
            String amountS = scanner.nextLine().trim().replace(',', '.');
            BigDecimal amount = new BigDecimal(amountS);

            Expense e = new Expense(date, category, description, amount);
            service.addExpense(e);
            System.out.println("Expense added.");
        } catch (DateTimeParseException dtpe) {
            System.out.println("Invalid date format. Use dd/MM/yyyy.");
        } catch (IOException io) {
            System.out.println("Unable to save expense: " + io.getMessage());
        } catch(NumberFormatException ex) {
        	System.out.println("Invalid amount format. Decimal is expected.");
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
