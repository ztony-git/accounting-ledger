package com.pluralsight;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private LocalDate date;
    private LocalTime time;
    private String description;
    private String vendor;
    private double amount;

    public Transaction(LocalDate date, LocalTime time, String description, String vendor, double amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
    }

    public LocalDate getLocalDate() {
        return date;
    }

    public void setLocalDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getLocalTime() {
        return time;
    }

    public void setLocalTime(LocalTime time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String toCSV() {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "|"
                + time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "|"
                + description + "|" + vendor + "|" + String.format("%.2f", amount) + "\n";
    }

    @Override
    public String toString() {
        return String.format("%-12s%-10s%-40s%-25s$%.2f",
                getLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                getLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                getDescription(), getVendor(), getAmount());
    }
}
