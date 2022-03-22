package com.vincent.forexledger.model.book;

import com.vincent.forexledger.model.CurrencyType;
import io.swagger.v3.oas.annotations.media.Schema;

public class BookListResponse {
    @Schema(description = "Id of the book.")
    private String id;

    @Schema(description = "Name of the book.")
    private String name;

    @Schema(description = "The currency type of transaction records in this book.", example = "USD")
    private CurrencyType currencyType;

    @Schema(description = "The balance of foreign currency.")
    private double balance;

    @Schema(description = "The profit calculated from transaction history. Unit: TWD.")
    private Integer twdProfit;

    @Schema(description = "The profit rate calculated from transaction history.", example = "-0.1234")
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
