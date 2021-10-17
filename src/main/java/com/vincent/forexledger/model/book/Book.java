package com.vincent.forexledger.model.book;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "book")
public class Book {
    @Id
    private String id;
    private String name;
    private BankType bank;
    private CurrencyType currencyType;
    private double balance;
    private int remainingTwdFund;
    private Double breakEvenPoint;
    private Double lastForeignInvest;
    private Integer lastTwdInvest;
    private String creator;
    private Date createdTime;

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

    public BankType getBank() {
        return bank;
    }

    public void setBank(BankType bank) {
        this.bank = bank;
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

    public int getRemainingTwdFund() {
        return remainingTwdFund;
    }

    public void setRemainingTwdFund(int remainingTwdFund) {
        this.remainingTwdFund = remainingTwdFund;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
