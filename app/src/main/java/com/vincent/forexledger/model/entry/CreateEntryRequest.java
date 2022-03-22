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

    private String description;

    @Positive
    private double foreignAmount;
    private Integer twdAmount;
    private String relatedBookId;
    private Double relatedBookForeignAmount;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getForeignAmount() {
        return foreignAmount;
    }

    public void setForeignAmount(double foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    public Integer getTwdAmount() {
        return twdAmount;
    }

    public void setTwdAmount(Integer twdAmount) {
        this.twdAmount = twdAmount;
    }

    public String getRelatedBookId() {
        return relatedBookId;
    }

    public void setRelatedBookId(String relatedBookId) {
        this.relatedBookId = relatedBookId;
    }

    public Double getRelatedBookForeignAmount() {
        return relatedBookForeignAmount;
    }

    public void setRelatedBookForeignAmount(Double relatedBookForeignAmount) {
        this.relatedBookForeignAmount = relatedBookForeignAmount;
    }
}
