package com.pluralsight;

import java.sql.Time;
import java.util.Date;

public class Transaction {
    private Date date;
    private Time time;
    private String paymentName;
    private String vendor;
    private double amount;

    public Transaction(Date date, Time time, String paymentName, String vendor, double amount) {
        this.date = date;
        this.time = time;
        this.paymentName = paymentName;
        this.vendor = vendor;
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
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
