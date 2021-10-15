package com.vincent.forexledger.model.book;

import com.vincent.forexledger.model.CurrencyType;

public class BookDetailResponse {
    private String id;
    private CurrencyType currencyType;
    private double bankSellingRate;
    private double bankBuyingRate;
    private double balance;
    private int twdCurrentValue; // balance * bankBuyingRate
    private int twdProfit; // calc
    private double twdProfitRate; // calc
    private Double breakEvenPoint; // calc
    private Double foreignLastInvest;
    private Integer twdLastInvest;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public double getBankSellingRate() {
        return bankSellingRate;
    }

    public void setBankSellingRate(double bankSellingRate) {
        this.bankSellingRate = bankSellingRate;
    }

    public double getBankBuyingRate() {
        return bankBuyingRate;
    }

    public void setBankBuyingRate(double bankBuyingRate) {
        this.bankBuyingRate = bankBuyingRate;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getTwdCurrentValue() {
        return twdCurrentValue;
    }

    public void setTwdCurrentValue(int twdCurrentValue) {
        this.twdCurrentValue = twdCurrentValue;
    }

    public int getTwdProfit() {
        return twdProfit;
    }

    public void setTwdProfit(int twdProfit) {
        this.twdProfit = twdProfit;
    }

    public double getTwdProfitRate() {
        return twdProfitRate;
    }

    public void setTwdProfitRate(double twdProfitRate) {
        this.twdProfitRate = twdProfitRate;
    }

    public Double getBreakEvenPoint() {
        return breakEvenPoint;
    }

    public void setBreakEvenPoint(Double breakEvenPoint) {
        this.breakEvenPoint = breakEvenPoint;
    }

    public Double getForeignLastInvest() {
        return foreignLastInvest;
    }

    public void setForeignLastInvest(Double foreignLastInvest) {
        this.foreignLastInvest = foreignLastInvest;
    }

    public Integer getTwdLastInvest() {
        return twdLastInvest;
    }

    public void setTwdLastInvest(Integer twdLastInvest) {
        this.twdLastInvest = twdLastInvest;
    }
}
