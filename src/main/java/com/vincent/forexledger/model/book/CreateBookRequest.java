package com.vincent.forexledger.model.book;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateBookRequest {

    @Schema(description = "The name of book.")
    @NotBlank
    private String name;

    @Schema(description = "The bank which transaction will happen to. It'll be used to identify exchange rate.", example = "RICHART")
    @NotNull
    private BankType bank;

    @Schema(description = "The currency type of transaction records in this book.", example = "USD")
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
