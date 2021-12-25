package com.vincent.forexledger.model;

import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.entry.TransactionType;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;
import java.util.List;

// TODO: provide swagger document of this request and response body
public class BookAndEntryBackup {
    @Valid
    @NotNull
    private BookBackup book;

    @Valid
    @NotNull
    private List<EntryBackup> entries;

    public BookBackup getBook() {
        return book;
    }

    public void setBook(BookBackup book) {
        this.book = book;
    }

    public List<EntryBackup> getEntries() {
        return entries;
    }

    public void setEntries(List<EntryBackup> entries) {
        this.entries = entries;
    }

    private static class BookBackup {
        @NotBlank
        private String name;

        @NotNull
        private BankType bank;

        @NotNull
        private CurrencyType currencyType;

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
    }

    private static class EntryBackup {
        @NotNull
        private TransactionType transactionType;

        @NotNull
        private Date transactionDate;

        @Positive
        private double foreignAmount;

        private Integer twdAmount;
        private String description;
        private String relatedBookId;
        private Double relatedBookForeignAmount;

        @NotNull
        private Date createdTime;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
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

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }
    }
}
