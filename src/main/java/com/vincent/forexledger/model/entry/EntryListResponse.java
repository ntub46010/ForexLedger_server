package com.vincent.forexledger.model.entry;

import com.vincent.forexledger.model.CurrencyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public class EntryListResponse {

    @Schema(description = "The id of entry.")
    private String id;

    @Schema(description = "The date when transaction occurred.")
    private Date transactionDate;

    @Schema(description = "The type of transaction.", example = "TRANSFER_IN_FROM_TWD")
    private TransactionType transactionType;

    @Schema(description = "The amount added or reduced to this book.")
    private double primaryAmount;

    @Schema(description = "The currency type of this book.", example = "USD")
    private CurrencyType primaryCurrencyType;

    @Schema(description = "The transfer amount of TWD. Or the transfer amount of related book when user creating entry.")
    private Double relatedAmount;

    @Schema(description = "The currency type of related book.", example = "JPY")
    private CurrencyType relatedCurrencyType;

    @Schema(description = "The statement to describe to this transaction")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public double getPrimaryAmount() {
        return primaryAmount;
    }

    public void setPrimaryAmount(double primaryAmount) {
        this.primaryAmount = primaryAmount;
    }

    public CurrencyType getPrimaryCurrencyType() {
        return primaryCurrencyType;
    }

    public void setPrimaryCurrencyType(CurrencyType primaryCurrencyType) {
        this.primaryCurrencyType = primaryCurrencyType;
    }

    public Double getRelatedAmount() {
        return relatedAmount;
    }

    public void setRelatedAmount(Double relatedAmount) {
        this.relatedAmount = relatedAmount;
    }

    public CurrencyType getRelatedCurrencyType() {
        return relatedCurrencyType;
    }

    public void setRelatedCurrencyType(CurrencyType relatedCurrencyType) {
        this.relatedCurrencyType = relatedCurrencyType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
