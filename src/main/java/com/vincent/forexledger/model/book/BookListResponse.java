package com.vincent.forexledger.model.book;

import com.vincent.forexledger.model.CurrencyType;

public class BookListResponse {
    private String id;
    private String name;
    private CurrencyType currencyType;
    private double balance;
    private Integer twdProfit;
    private Double profitRate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Integer getTwdProfit() {
        return twdProfit;
    }

    public void setTwdProfit(Integer twdProfit) {
        this.twdProfit = twdProfit;
    }

    public Double getProfitRate() {
        return profitRate;
    }

    public void setProfitRate(Double profitRate) {
        this.profitRate = profitRate;
    }
}
