package com.pluralsight;

import java.time.LocalTime;
import java.time.LocalDate;

public class Transaction {
    private LocalDate date;
    private LocalTime time;
    private String paymentName;
    private String vendor;
    private double amount;

    public Transaction(LocalDate date, LocalTime time, String paymentName, String vendor, double amount) {
        this.date = date;
        this.time = time;
        this.paymentName = paymentName;
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

    public String getPaymentName() {
        return paymentName;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
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
}
