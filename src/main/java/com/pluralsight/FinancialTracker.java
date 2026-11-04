package com.pluralsight;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Scanner;

/*
 * Capstone skeleton – personal finance tracker.
 * ------------------------------------------------
 * File format  (pipe-delimited)
 *     yyyy-MM-dd|HH:mm:ss|description|vendor|amount
 * A deposit has a positive amount; a payment is stored
 * as a negative amount.
 *
 *       create the Transaction class in this package first.
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
//         customSearchMenu(scanner);

        while (running) {
            separator();
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
        //       create file if it does not exist, then read each line,
        //       parse the five fields, build a Transaction object,
        //       and add it to the transactions list.
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            String line;

            while((line = reader.readLine()) != null) { // create and fill transactions arraylist with loaded .csv
                String[] data = line.split("\\|");

                LocalDate date = LocalDate.parse(data[0], DATE_FMT);
                LocalTime time = LocalTime.parse(data[1], TIME_FMT);

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
                System.out.print("Enter the date and time (yyyy-MM-dd HH:mm:ss). X to return home: ");
                String in = scanner.nextLine();
                if (in.equalsIgnoreCase("X")) return null;

                dateTime = LocalDateTime.parse(in, DATETIME_FMT);
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
        if (transaction == null) return;

        System.out.println("Deposit on " + transaction.getLocalDate().format(DATE_FMT)
                + " " +  transaction.getLocalTime().format(TIME_FMT)
                + " of $" + transaction.getAmount()
                + " for " + transaction.getDescription()
                + " by " + transaction.getVendor()
                + " logged.");

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
        if (transaction == null) return;

        transaction.setAmount(-transaction.getAmount());


        System.out.println("Payment on " + transaction.getLocalDate().format(DATE_FMT)
                + " " +  transaction.getLocalTime().format(TIME_FMT)
                + " of $" + transaction.getAmount()
                + " for " + transaction.getDescription()
                + " by " + transaction.getVendor()
                + " logged.");
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
            separator();
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
    private static void separator() {
        System.out.println("-----------------------------------------------------------------");
    }

    private static void displayHeader() {
        System.out.printf("%-12s%-10s%-40s%-25s%s%n", "Date", "Time", "Description", "Vendor", "Amount");
    }

    private static void displayLedger() {
        separator();
        displayHeader();
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    private static void displayDeposits() {
        separator();
        displayHeader();
        for (Transaction transaction : transactions) {
            if(transaction.getAmount() >= 0) {
                System.out.println(transaction);
            }
        }
    }

    private static void displayPayments() {
        separator();
        displayHeader();
        for (Transaction transaction : transactions) {
            if(transaction.getAmount() < 0) {
                System.out.println(transaction);
            }
        }
    }

    private static void displayTransactions(ArrayList<Transaction> transactions) {
        separator();
        displayHeader();
        transactions.forEach(System.out::println);
    }
    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            separator();
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
                case "1" -> { // month-to-date report
                    int month = LocalDate.now().getMonthValue();
                    int year = LocalDate.now().getYear();

                    LocalDate start = LocalDate.of(year, month, 1); // start of this month
                    ArrayList<Transaction> filteredTransactions = filterTransactionsByDate(new String[]{
                            start.toString(), "", "Inclusive"
                    }, transactions);

                    displayTransactions(filteredTransactions);
                }
                case "2" -> { // previous month report
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
                    ArrayList<Transaction> filteredTransactions = filterTransactionsByDate(new String[]{
                            start.toString(), end.toString(), "Inclusive"
                    }, transactions);

                    displayTransactions(filteredTransactions);
                }
                case "3" -> { // year-to-date report
                    int year = LocalDate.now().getYear();

                    LocalDate start = LocalDate.of(year, 1, 1); // start of year
                    ArrayList<Transaction> filteredTransactions = filterTransactionsByDate(new String[]{
                            start.toString(), "", "Inclusive"
                    }, transactions);

                    displayTransactions(filteredTransactions);
                }
                case "4" -> { // previous year report
                    int year =  LocalDate.now().getYear() - 1;

                    LocalDate start = LocalDate.of(year, 1, 1);
                    LocalDate end = LocalDate.of(year, 12, 31);
                    ArrayList<Transaction> filteredTransactions = filterTransactionsByDate(new String[]{
                            start.toString(), end.toString(), "Inclusive"
                    }, transactions);

                    displayTransactions(filteredTransactions);
                }
                case "5" -> { // prompt for vendor then report
                    System.out.print("Enter Vendor: ");
                    String vendor = scanner.nextLine();

                    ArrayList<Transaction> filteredTransactions = filterTransactionsByVendor(new String[]{
                            vendor, "Loose"}, transactions);
                    displayTransactions(filteredTransactions);
                }
                case "6" -> customSearchMenu(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Reporting helpers
       ------------------------------------------------------------------ */
    private static ArrayList<Transaction> filterTransactionsByDate(String[] dateConfig, ArrayList<Transaction> transactions) {
        // iterate transactions, print those within the range
        ArrayList<Transaction> filteredTransactions = new ArrayList<>();
        LocalDate start = null;
        LocalDate end = null;

        // if no set dates, don't filter
        if (dateConfig[0].equalsIgnoreCase("") && dateConfig[1].equalsIgnoreCase(""))
            return transactions;

        // if both set dates, filter from set dates
        if (!dateConfig[0].equalsIgnoreCase("") && !dateConfig[1].equalsIgnoreCase("")) {
            start = LocalDate.parse(dateConfig[0]);
            end = LocalDate.parse(dateConfig[1]);
        }

        // if only first set date, assume filter from first date until today
        if (!dateConfig[0].equalsIgnoreCase("") && dateConfig[1].equalsIgnoreCase("")) {
            start = LocalDate.parse(dateConfig[0]);
            end = LocalDate.now();
        }

        // if only second set date, filter until second date
        if (dateConfig[0].equalsIgnoreCase("") && !dateConfig[1].equalsIgnoreCase("")) {
            start = LocalDate.of(0, 1,1);
            end = LocalDate.parse(dateConfig[1]);
        }


        // search loop
        switch (dateConfig[2].toLowerCase()) {
            case "inclusive" -> {
                for (Transaction transaction : transactions) {
                    if (!transaction.getLocalDate().isAfter(end) && !transaction.getLocalDate().isBefore(start)) { // uses not to make check inclusive
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "exclusive" -> {
                for (Transaction transaction : transactions) {
                    if (transaction.getLocalDate().isAfter(start) && transaction.getLocalDate().isBefore(end)) { // uses not to make check inclusive
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "inverse" -> {
                for (Transaction transaction : transactions) {
                    if (transaction.getLocalDate().isAfter(end) || transaction.getLocalDate().isBefore(start)) { // uses not to make check inclusive
                        filteredTransactions.add(transaction);
                    }
                }
            }
        }

        return  filteredTransactions;
    }

    private static ArrayList<Transaction> filterTransactionsByTime(String[] timeConfig, ArrayList<Transaction> transactions) {
        // iterate transactions, print those within the range
        ArrayList<Transaction> filteredTransactions = new ArrayList<>();
        LocalTime start = null;
        LocalTime end = null;

        // if no set times, don't filter
        if (timeConfig[0].equalsIgnoreCase("") && timeConfig[1].equalsIgnoreCase(""))
            return transactions;
        // if both set times, filter from set times
        if (!timeConfig[0].equalsIgnoreCase("") && !timeConfig[1].equalsIgnoreCase("")) {
            start = LocalTime.parse(timeConfig[0], TIME_FMT);
            end = LocalTime.parse(timeConfig[1], TIME_FMT);
        }
        // if only first set time, assume filter from first time until current time
        if (!timeConfig[0].equalsIgnoreCase("") && timeConfig[1].equalsIgnoreCase("")) {
            start = LocalTime.parse(timeConfig[0], TIME_FMT);
            end = LocalTime.now();
        }
        // if only second set time, filter until second time
        if (timeConfig[0].equalsIgnoreCase("") && !timeConfig[1].equalsIgnoreCase("")) {
            start = LocalTime.of(0, 0,0);
            end = LocalTime.parse(timeConfig[1], TIME_FMT);
        }

        // search loop
        switch (timeConfig[2].toLowerCase()) {
            case "inclusive" -> {
                for (Transaction transaction : transactions) {
                    if (!transaction.getLocalTime().isAfter(end) && !transaction.getLocalTime().isBefore(start)) { // uses not to make check inclusive
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "exclusive" -> {
                for (Transaction transaction : transactions) {
                    if (transaction.getLocalTime().isAfter(start) && transaction.getLocalTime().isBefore(end)) { // uses not to make check inclusive
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "inverse" -> {
                for (Transaction transaction : transactions) {
                    if (transaction.getLocalTime().isAfter(end) || transaction.getLocalTime().isBefore(start)) { // uses not to make check inclusive
                        filteredTransactions.add(transaction);
                    }
                }
            }
        }

        return  filteredTransactions;
    }

    private static ArrayList<Transaction> filterTransactionsByDesc(String[] descConfig, ArrayList<Transaction> transactions) {
        // iterate transactions, print those within the range
        ArrayList<Transaction> filteredTransactions = new ArrayList<>();

        // if no set desc, don't filter
        if (descConfig[0].equalsIgnoreCase(""))
            return transactions;

        // search loop
        switch (descConfig[1].toLowerCase()) {
            case "loose" -> {
                for (Transaction transaction : transactions) {
                    if (transaction.getDescription().toLowerCase().contains(descConfig[0])) {
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "strict" -> {
                for (Transaction transaction : transactions) {
                    if (transaction.getDescription().equalsIgnoreCase(descConfig[0])) {
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "inverse loose" -> {
                for (Transaction transaction : transactions) {
                    if (!transaction.getDescription().toLowerCase().contains(descConfig[0])) {
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "inverse strict" -> {
                for (Transaction transaction : transactions) {
                    if (!transaction.getDescription().equalsIgnoreCase(descConfig[0])) {
                        filteredTransactions.add(transaction);
                    }
                }
            }
        }

        return  filteredTransactions;
    }

    private static ArrayList<Transaction> filterTransactionsByVendor(String[] vendorConfig, ArrayList<Transaction> transactions) {
        // iterate transactions, print those within the range
        ArrayList<Transaction> filteredTransactions = new ArrayList<>();

        // if no set vendor, don't filter
        if (vendorConfig[0].equalsIgnoreCase(""))
            return transactions;

        // search loop
        switch (vendorConfig[1].toLowerCase()) {
            case "loose" -> {
                for (Transaction transaction : transactions) {
                    if (transaction.getVendor().toLowerCase().contains(vendorConfig[0])) {
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "strict" -> {
                for (Transaction transaction : transactions) {
                    if (transaction.getVendor().equalsIgnoreCase(vendorConfig[0])) {
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "inverse loose" -> {
                for (Transaction transaction : transactions) {
                    if (!transaction.getVendor().toLowerCase().contains(vendorConfig[0])) {
                        filteredTransactions.add(transaction);
                    }
                }
            }
            case "inverse strict" -> {
                for (Transaction transaction : transactions) {
                    if (!transaction.getVendor().equalsIgnoreCase(vendorConfig[0])) {
                        filteredTransactions.add(transaction);
                    }
                }
            }
        }

        return  filteredTransactions;
    }

    private static ArrayList<Transaction> filterTransactionsByAmount(String[] amountConfig, ArrayList<Transaction> transactions) {
        // iterate transactions, print those within the range
        ArrayList<Transaction> filteredTransactions = new ArrayList<>();

        // if no set vendor, don't filter
        if (amountConfig[0].equalsIgnoreCase(""))
            return transactions;

        double amount = parseDouble(amountConfig[0]);

        // search loop
        if (parseDouble(amountConfig[1]) != null) {
            double min = amount * (1 - (parseDouble(amountConfig[1]) * .01));
            double max = amount * (1 + (parseDouble(amountConfig[1]) * .01));

            for (Transaction transaction : transactions) {
                double fixedAmount = amountConfig[2].equals("true") ? Math.abs(transaction.getAmount()) : transaction.getAmount();

                if (fixedAmount >= min && fixedAmount <= max) {
                    filteredTransactions.add(transaction);
                }
            }
        } else {
            switch (amountConfig[1].toLowerCase()) {
                case ">" -> {
                    for (Transaction transaction : transactions) {
                        double fixedAmount = amountConfig[2].equals("true") ? Math.abs(transaction.getAmount()) : transaction.getAmount();

                        if (fixedAmount > amount) {
                            filteredTransactions.add(transaction);
                        }
                    }
                }
                case "<" -> {
                    for (Transaction transaction : transactions) {
                        double fixedAmount = amountConfig[2].equals("true") ? Math.abs(transaction.getAmount()) : transaction.getAmount();

                        if (fixedAmount < amount) {
                            filteredTransactions.add(transaction);
                        }
                    }
                }
            }
        }

        return  filteredTransactions;
    }

    /* ------------------------------------------------------------------
       Custom Search
       ------------------------------------------------------------------ */
    private static void customSearchMenu(Scanner scanner) {
        String[] dateConfig = {"", "", "Inclusive"};
        String[] timeConfig = {"", "", "Inclusive"};
        String[] descConfig = {"", "Loose"};
        String[] vendorConfig = {"", "Loose"};
        String[] amountConfig = {"", "0", "false"};

        boolean running = true;

        while (running) {
            separator();
            System.out.println("Custom Search");
            System.out.println("Customize your query:");

            System.out.println("1) Edit Start & End Date");
            System.out.printf("   %-14s: %-11s%n" +
                              "   %-14s: %-11s%n" +
                              "   %-14s: %-11s%n",
                    "> Start Date", dateConfig[0].equalsIgnoreCase("") ? "N/A" : dateConfig[0],
                    "> End Date", dateConfig[1].equalsIgnoreCase("") ? "N/A" : dateConfig[1],
                    "> Search Type", dateConfig[2]);

            System.out.println("2) Edit Start & End Time");
            System.out.printf("   %-14s: %-9s%n" +
                              "   %-14s: %-9s%n" +
                              "   %-14s: %-9s%n",
                    "> Start Time", timeConfig[0].equalsIgnoreCase("") ? "N/A" : timeConfig[0],
                    "> End Time", timeConfig[1].equalsIgnoreCase("") ? "N/A" : timeConfig[1],
                    "> Search Type", timeConfig[2]);

            System.out.println("3) Edit Description");
            System.out.printf("   %-14s: %-9s%n" +
                              "   %-14s: %-9s%n",
                    "> Description", descConfig[0].equalsIgnoreCase("") ? "N/A" : descConfig[0],
                    "> Search Type", descConfig[1]);

            System.out.println("4) Edit Vendor");
            System.out.printf("   %-14s: %-9s%n" +
                              "   %-14s: %-9s%n",
                    "> Vendor", vendorConfig[0].equalsIgnoreCase("") ? "N/A" : vendorConfig[0],
                    "> Search Type", vendorConfig[1]);

            System.out.println("5) Edit Amount");
            System.out.printf("   %-14s: %-9s%n" +
                              "   %-14s: %-9s%n" +
                              "   %-14s: %-9s%n",
                    "> Amount", amountConfig[0].equalsIgnoreCase("") ? "N/A" : amountConfig[0],
                    "> Search Type", parseDouble(amountConfig[1]) == null ? amountConfig[1] : amountConfig[1] + "%",
                    "> Absolute", amountConfig[2]);

            System.out.println("6) Search");

            System.out.println("0) Return");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> { // Start & End date, Inclusive v Exclusive v Inverse
                    queryDate(scanner, dateConfig);
                }
                case "2" -> { // Start & End time, Inclusive v Exclusive
                    queryTime(scanner, timeConfig);
                }
                case "3" -> { // Description Strict vs Loose
                    queryDesc(scanner, descConfig);
                }
                case "4" -> {// Vendor Strict vs Loose
                    queryVendor(scanner, vendorConfig);
                }
                case "5" -> {// Amount >, <, = within % range
                    queryAmount(scanner, amountConfig);
                }
                case "6" -> {// Search
                    customSearch(dateConfig, timeConfig, descConfig, vendorConfig, amountConfig);
                }
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    private static void queryDate(Scanner scanner, String[] dateConfig) {
        separator();
        System.out.println("Editing date query:");

        System.out.print("Start Date (yyyy-MM-dd): ");
        String startDate = scanner.nextLine();
        if (parseDate(startDate) == null && !startDate.isEmpty()) {
            System.out.println("Invalid date");
            return;
        }

        System.out.print("End Date (yyyy-MM-dd): ");
        String endDate = scanner.nextLine();
        if (parseDate(endDate) == null  && !endDate.isEmpty()) {
            System.out.println("Invalid date");
            return;
        }

        if (startDate.isEmpty() && endDate.isEmpty()) {
            System.out.println("Removing Date Query...");
            dateConfig[0] = startDate;
            dateConfig[1] = endDate;
            return;
        }

        System.out.print("Search Type (Inclusive/Exclusive/Inverse): ");
        String searchType = scanner.nextLine();
        if (!searchType.equalsIgnoreCase("Inclusive")
                && !searchType.equalsIgnoreCase("Exclusive")
                && !searchType.equalsIgnoreCase("Inverse")
                && !searchType.isEmpty()) {
            System.out.println("Invalid search type");
            return;
        }

        System.out.println("Updating Date Query...");
        dateConfig[0] = startDate;
        dateConfig[1] = endDate;

        // if no input use the same search type
        dateConfig[2] = searchType.isEmpty() ? dateConfig[2]
                        : searchType.substring(0, 1).toUpperCase() + searchType.substring(1).toLowerCase();
    }

    private static void queryTime(Scanner scanner, String[] timeConfig) {
        separator();
        System.out.println("Editing time query:");

        System.out.print("Start Time (hh:mm:ss): ");
        String startTime = scanner.nextLine();
        if (parseTime(startTime) == null && !startTime.isEmpty()) {
            System.out.println("Invalid time");
            return;
        }

        System.out.print("End Time (hh:mm:ss): ");
        String endTime = scanner.nextLine();
        if (parseTime(endTime) == null && !endTime.isEmpty()) {
            System.out.println("Invalid time");
            return;
        }

        if (startTime.isEmpty() && endTime.isEmpty()) {
            System.out.println("Removing Time Query...");
            timeConfig[0] = startTime;
            timeConfig[1] = endTime;
            return;
        }

        System.out.print("Search Type (Inclusive/Exclusive/Inverse): ");
        String searchType = scanner.nextLine();
        if (!searchType.equalsIgnoreCase("Inclusive")
                && !searchType.equalsIgnoreCase("Exclusive")
                && !searchType.equalsIgnoreCase("Inverse")
                && !searchType.isEmpty()) {
            System.out.println("Invalid search type");
            return;
        }

        System.out.println("Updating Time Query...");
        timeConfig[0] = startTime;
        timeConfig[1] = endTime;
        // if no input use the same search type
        timeConfig[2] = searchType.isEmpty() ? timeConfig[2]
                : searchType.substring(0, 1).toUpperCase() + searchType.substring(1).toLowerCase();
    }

    private static void queryDesc(Scanner scanner, String[] descConfig) {
        separator();
        System.out.println("Editing description query:");

        System.out.print("Description: ");
        String desc = scanner.nextLine();

        if (desc.isEmpty()) {
            System.out.println("Removing Description Query...");
            descConfig[0] = desc;
            return;
        }

        System.out.print("Search Type (Loose/Strict/Inverse loose/Inverse strict): ");
        String searchType = scanner.nextLine();
        if (!searchType.equalsIgnoreCase("Loose")
                && !searchType.equalsIgnoreCase("Strict")
                && !searchType.equalsIgnoreCase("Inverse Loose")
                && !searchType.equalsIgnoreCase("Inverse Strict")
                && !searchType.isEmpty()) {
            System.out.println("Invalid search type");
            return;
        }

        System.out.println("Updating Description Query...");
        descConfig[0] = desc;
        // if no input use the same search type
        descConfig[1] = searchType.isEmpty() ? descConfig[1]
                : searchType.substring(0, 1).toUpperCase() + searchType.substring(1).toLowerCase();
    }

    private static void queryVendor(Scanner scanner, String[] vendorConfig) {
        separator();
        System.out.println("Editing vendor query:");

        System.out.print("Vendor: ");
        String vendor = scanner.nextLine();

        if (vendor.isEmpty()) {
            System.out.println("Removing Vendor Query...");
            vendorConfig[0] = vendor;
            return;
        }

        System.out.print("Search Type (Loose/Strict/Inverse loose/Inverse strict): ");
        String searchType = scanner.nextLine();
        if (!searchType.equalsIgnoreCase("Loose")
                && !searchType.equalsIgnoreCase("Strict")
                && !searchType.equalsIgnoreCase("Inverse Loose")
                && !searchType.equalsIgnoreCase("Inverse Strict")
                && !searchType.isEmpty()) {
            System.out.println("Invalid search type");
            return;
        }

        System.out.println("Updating Vendor Query...");
        vendorConfig[0] = vendor;
        // if no input use the same search type
        vendorConfig[1] = searchType.isEmpty() ? vendorConfig[1]
                : searchType.substring(0, 1).toUpperCase() + searchType.substring(1).toLowerCase();
    }

    private static void queryAmount(Scanner scanner, String[] amountConfig) {
        separator();
        System.out.println("Editing amount query:");

        System.out.print("Amount: ");
        String amount = scanner.nextLine();

        if (amount.isEmpty()) {
            System.out.println("Removing Amount Query...");
            amountConfig[0] = amount;
            return;
        }

        System.out.print("Search Type (</>/=%): ");
        String searchType = scanner.nextLine();
        if (!searchType.equalsIgnoreCase("<") && !searchType.equalsIgnoreCase(">")
                && !searchType.equalsIgnoreCase("=") && !searchType.isEmpty()) {
            System.out.println("Invalid search type");
            return;
        } else if (searchType.equalsIgnoreCase("=")) {
            System.out.print("Range % (number only): ");
            searchType = scanner.nextLine();
            searchType = searchType.replace("%", "");
            searchType = searchType.replace(" ", "");
            if(parseDouble(searchType) == null) {
                System.out.println("Invalid range");
                return;
            }
        }

        System.out.print("Absolute Value (true/false): ");
        String abs = scanner.nextLine();
        if (!abs.isEmpty() && !abs.equalsIgnoreCase("true") && !abs.equalsIgnoreCase("false")) {
            System.out.println("Invalid absolute value type");
            return;
        }

        System.out.println("Updating Amount Query...");
        amountConfig[0] = amount;
        amountConfig[1] = searchType.isEmpty() ? amountConfig[1]
                : searchType.substring(0, 1).toUpperCase() + searchType.substring(1).toLowerCase();
        amountConfig[2] = abs.isEmpty() ? amountConfig[2] : abs.toLowerCase();
    }

    private static void customSearch(String[] dateConfig, String[] timeConfig, String[] descConfig, String[] vendorConfig, String[] amountConfig) {
        // TODO – prompt for any combination of date range, description,
        //        vendor, and exact amount, then display matches
        ArrayList<Transaction> filteredTransactions = new ArrayList<>(transactions);
        filteredTransactions = filterTransactionsByDate(dateConfig, filteredTransactions);
        filteredTransactions = filterTransactionsByTime(timeConfig, filteredTransactions);
        filteredTransactions = filterTransactionsByDesc(descConfig, filteredTransactions);
        filteredTransactions = filterTransactionsByVendor(vendorConfig, filteredTransactions);
        filteredTransactions = filterTransactionsByAmount(amountConfig, filteredTransactions);

        displayTransactions(filteredTransactions);
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalTime parseTime(String s) {
        try {
            return LocalTime.parse(s, TIME_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
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
