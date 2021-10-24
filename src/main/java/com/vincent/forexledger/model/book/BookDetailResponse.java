package com.vincent.forexledger.model.book;

import com.vincent.forexledger.model.CurrencyType;

public class BookDetailResponse {
    private String id;
    private CurrencyType currencyType;
    private double bankBuyingRate;
    private double balance;
    private int twdCurrentValue;
    private Integer twdProfit;
    private Double twdProfitRate;
    private Double breakEvenPoint;
    private Double lastForeignInvest;
    private Integer lastTwdInvest;
    private Double lastSellingRate;

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

    public Integer getTwdProfit() {
        return twdProfit;
    }

    public void setTwdProfit(Integer twdProfit) {
        this.twdProfit = twdProfit;
    }

    public Double getTwdProfitRate() {
        return twdProfitRate;
    }

    public void setTwdProfitRate(Double twdProfitRate) {
        this.twdProfitRate = twdProfitRate;
    }

    public Double getBreakEvenPoint() {
        return breakEvenPoint;
    }

    public void setBreakEvenPoint(Double breakEvenPoint) {
        this.breakEvenPoint = breakEvenPoint;
    }

    public Double getLastForeignInvest() {
        return lastForeignInvest;
    }

    public void setLastForeignInvest(Double lastForeignInvest) {
        this.lastForeignInvest = lastForeignInvest;
    }

    public Integer getLastTwdInvest() {
        return lastTwdInvest;
    }

    public void setLastTwdInvest(Integer lastTwdInvest) {
        this.lastTwdInvest = lastTwdInvest;
    }

    public Double getLastSellingRate() {
        return lastSellingRate;
    }

    public void setLastSellingRate(Double lastSellingRate) {
        this.lastSellingRate = lastSellingRate;
    }
}
