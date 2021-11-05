package com.vincent.forexledger.model.entry;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class Entry {
    @Id
    private String id;
    private String bookId;
    private TransactionType transactionType;
    private Date transactionDate;
    private double foreignAmount;
    private Integer twdAmount;
    private String relatedBookId;
    private Double relatedForeignAmount;
    private String creator;
    private Date createdTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Double getRelatedForeignAmount() {
        return relatedForeignAmount;
    }

    public void setRelatedForeignAmount(Double relatedForeignAmount) {
        this.relatedForeignAmount = relatedForeignAmount;
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
