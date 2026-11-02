package com.pluralsight;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

/*
 * Capstone skeleton – personal finance tracker.
 * ------------------------------------------------
 * File format  (pipe-delimited)
 *     yyyy-MM-dd|HH:mm:ss|description|vendor|amount
 * A deposit has a positive amount; a payment is stored
 * as a negative amount.
 *
 * TODO: create the Transaction class in this package first.
 *       One object = one line of the file. Decide its fields from
 *       the file format above, add a constructor and getters.
 *       This class does not compile until Transaction exists.
 */
public class FinancialTracker {

    /* ------------------------------------------------------------------
       Shared data and formatters
       ------------------------------------------------------------------ */
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /* ------------------------------------------------------------------
       Main menu
       ------------------------------------------------------------------ */
    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        // sort transactions by newest first
        sortByDateTime(transactions);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        scanner.close();
    }

    /* ------------------------------------------------------------------
       File I/O
       ------------------------------------------------------------------ */

    /**
     * Load transactions from FILE_NAME.
     * • If the file doesn’t exist, create an empty one so that future writes succeed.
     * • Each line looks like: date|time|description|vendor|amount
     */
    public static void loadTransactions(String fileName) {
        // TODO: create file if it does not exist, then read each line,
        //       parse the five fields, build a Transaction object,
        //       and add it to the transactions list.
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            String line;

            while((line = reader.readLine()) != null) { // create and fill transactions arraylist with loaded .csv
                String[] data = line.split("\\|");

                LocalDate date = LocalDate.parse(data[0], DateTimeFormatter.ofPattern(DATE_PATTERN));
                LocalTime time = LocalTime.parse(data[1], DateTimeFormatter.ofPattern(TIME_PATTERN));

                Transaction transaction = new Transaction(date, time, data[2], data[3], Double.parseDouble(data[4]));
                transactions.add(transaction);
            }
        } catch (Exception e) {
            try { // create .csv file
                System.out.println("Attempting to make transactions.csv file...");
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                writer.close();
            } catch (Exception ex) {
                System.out.println("Creating file failed: " + ex.getMessage());
            }
        }
    }

    /* ------------------------------------------------------------------
       Add new transactions
       ------------------------------------------------------------------ */

    private static Transaction addTransaction(Scanner scanner) {
        // datetime
        LocalDateTime dateTime = null;
        while (dateTime == null) { // keep asking until correct format is followed
            try {
                System.out.print("Enter the date and time (yyyy-MM-dd HH:mm:ss): ");
                dateTime = LocalDateTime.parse(scanner.nextLine(), DateTimeFormatter.ofPattern(DATETIME_PATTERN));
            }  catch (Exception e) {
                System.out.println("Invalid date and time: " + e.getMessage());
            }
        }
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        // description
        System.out.print("Enter payment description: ");
        String description = scanner.nextLine();

        // vendor
        System.out.print("Enter payment vendor: ");
        String vendor = scanner.nextLine();

        // amount
        double amount;
        do { // keep asking until transaction amount is positive
            System.out.print("Enter transaction amount: ");
            amount = scanner.nextDouble();
            scanner.nextLine();

            if (amount <= 0) {
                System.out.println("Must provide a positive amount.");
            }
        } while (amount <= 0);

        return new Transaction(date, time, description, vendor, amount);
    }

    /**
     * Prompt for ONE date+time string in the format
     * "yyyy-MM-dd HH:mm:ss", plus description, vendor, amount.
     * Validate that the amount entered is positive.
     * Store the amount as-is (positive) and append to the file.
     */
    private static void addDeposit(Scanner scanner) {
        Transaction transaction = addTransaction(scanner);
        transactions.add(transaction);
        sortByDateTime(transactions);
        appendTransaction(transaction);
    }

    /**
     * Same prompts as addDeposit.
     * Amount must be entered as a positive number,
     * then converted to a negative amount before storing.
     */
    private static void addPayment(Scanner scanner) {
        Transaction transaction = addTransaction(scanner);
        transaction.setAmount(-transaction.getAmount());
        transactions.add(transaction);
        sortByDateTime(transactions);
        appendTransaction(transaction);
    }

    /* ------------------------------------------------------------------
       Ledger menu
       ------------------------------------------------------------------ */
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Display helpers: show data in neat columns
       ------------------------------------------------------------------ */
    private static void displayHeader() {
        System.out.printf("%-12s%-10s%-40s%-25s%s%n", "Date", "Time", "Description", "Vendor", "Amount");
    }

    private static void displayLedger() {
        displayHeader();
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    private static void displayDeposits() {
        displayHeader();
        for (Transaction transaction : transactions) {
            if(transaction.getAmount() >= 0) {
                System.out.println(transaction);
            }
        }
    }

    private static void displayPayments() {
        displayHeader();
        for (Transaction transaction : transactions) {
            if(transaction.getAmount() < 0) {
                System.out.println(transaction);
            }
        }
    }

    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Reports");
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {/* TODO – month-to-date report */
                    int month = LocalDate.now().getMonthValue();
                    int year = LocalDate.now().getYear();

                    LocalDate start = LocalDate.of(year, month, 1); // start of this month
                    LocalDate end = LocalDate.now(); // today
                    filterTransactionsByDate(start, end);
                }
                case "2" -> {/* TODO – previous month report */
                    int currentMonth = LocalDate.now().getMonthValue();
                    int month = (currentMonth - 1) % 12;
                    int year;
                    if (currentMonth - 1 > 0) { // check last month was this year
                        year = LocalDate.now().getYear();
                    } else {
                        year = LocalDate.now().getYear() - 1;
                    }

                    LocalDate start = LocalDate.of(year, month, 1);  // start of prev month
                    LocalDate end = LocalDate.of(year, month, start.lengthOfMonth()); // end of prev month
                    filterTransactionsByDate(start, end);
                }
                case "3" -> {/* TODO – year-to-date report   */
                    int year = LocalDate.now().getYear();

                    LocalDate start = LocalDate.of(year, 1, 1); // start of year
                    LocalDate end = LocalDate.now(); // today
                    filterTransactionsByDate(start, end);
                }
                case "4" -> {/* TODO – previous year report  */
                    int year =  LocalDate.now().getYear() - 1;

                    LocalDate start = LocalDate.of(year, 1, 1);
                    LocalDate end = LocalDate.of(year, 12, 31);
                    filterTransactionsByDate(start, end);
                }
                case "5" -> {/* TODO – prompt for vendor then report */
                    System.out.print("Enter Vendor: ");
                    String vendor = scanner.nextLine();

                    filterTransactionsByVendor(vendor);
                }
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Reporting helpers
       ------------------------------------------------------------------ */
    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
        // TODO – iterate transactions, print those within the range
        displayHeader();
        for (Transaction transaction : transactions) {
            if (!transaction.getLocalDate().isAfter(end) && !transaction.getLocalDate().isBefore(start)) { // uses not to make check inclusive
                System.out.println(transaction);
            }
        }
    }

    private static void filterTransactionsByVendor(String vendor) {
        // TODO – iterate transactions, print those with matching vendor
        displayHeader();
        for (Transaction transaction : transactions) {
            if (transaction.getVendor().equalsIgnoreCase(vendor)) {
                System.out.println(transaction);
            }
        }
    }

    private static void customSearch(Scanner scanner) {
        // TODO – prompt for any combination of date range, description,
        //        vendor, and exact amount, then display matches
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        /* TODO – return LocalDate or null */
        return null;
    }

    private static Double parseDouble(String s) {
        /* TODO – return Double   or null */
        return null;
    }

    /* ------------------------------------------------------------------
       Utility
       ------------------------------------------------------------------ */
    private static void appendTransaction(Transaction transaction) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));
            writer.write(transaction.toCSV());
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void sortByDateTime(ArrayList<Transaction> transactions) {
        // sorts by newest first
        transactions.sort(Comparator.comparing(Transaction::getLocalDate).thenComparing(Transaction::getLocalTime).reversed());
    }
}
