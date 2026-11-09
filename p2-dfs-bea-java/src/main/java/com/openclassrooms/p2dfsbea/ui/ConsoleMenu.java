package com.openclassrooms.p2dfsbea.ui;

import com.openclassrooms.p2dfsbea.model.Expense;
import com.openclassrooms.p2dfsbea.repository.ExpenseRepository;
import com.openclassrooms.p2dfsbea.service.ExpenseService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

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
                case "2" -> showHistoryWithGrandTotal();
                case "3" -> addExpense();
                case "4" -> {
                    System.out.println("Goodbye");
                    running = false;
                }
                default -> System.out.println("Unknown option, please try again.");
            }
        }
    }

    /**
     * Show expenses history with Grand Total
     */
    private void showHistoryWithGrandTotal() {
        List<Expense> list = service.loadAllExpenses();
        System.out.println("\nHistory (most recent first):");
        BigDecimal grandTotal = list.stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .peek(e -> System.out.println(e.toCsvLine()))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("\n --- Grand Total: " + grandTotal + " ---");
    }

    private void printMenu() {
        System.out.println("\n--- Personal Finance Tracker ---");
        System.out.println("1) Show history");
        System.out.println("2) Show history (with Grand Total)");
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
