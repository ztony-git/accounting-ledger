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
        int[] rgbPos = {0, 255, 0};
        int[] rgbNeg = {255, 0, 0};

        return String.format("%-12s%-10s%-40s%-25s%s$%.2f\033[0m",
                getLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                getLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                getDescription(), getVendor(),
                getAmount() > 0 ? "\033[38;2;" + rgbPos[0] + ";" + rgbPos[1] + ";" + rgbPos[2] + "m" :
                    (getAmount() < 0 ? "\033[38;2;" + rgbNeg[0] + ";" + rgbNeg[1] + ";" + rgbNeg[2] + "m" :
                    ""),
                getAmount());
    }
}
