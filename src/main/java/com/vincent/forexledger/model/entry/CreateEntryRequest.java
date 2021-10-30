package com.vincent.forexledger.model.entry;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

public class CreateEntryRequest {
    @NotBlank
    private String bookId;

    @NotNull
    private TransactionType transactionType;

    @NotNull
    private Date transactionDate;

    @Positive
    private double foreignAmount;
    private Double twdAmount;
    private String anotherBookId;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getForeignAmount() {
        return foreignAmount;
    }

    public void setForeignAmount(double foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    public Double getTwdAmount() {
        return twdAmount;
    }

    public void setTwdAmount(Double twdAmount) {
        this.twdAmount = twdAmount;
    }

    public String getAnotherBookId() {
        return anotherBookId;
    }

    public void setAnotherBookId(String anotherBookId) {
        this.anotherBookId = anotherBookId;
    }
}
