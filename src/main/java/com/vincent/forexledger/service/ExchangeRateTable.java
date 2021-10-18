package com.vincent.forexledger.service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRateResponse;

import java.util.Collection;

public class ExchangeRateTable {
    private Table<BankType, CurrencyType, ExchangeRateResponse> table;

    public ExchangeRateTable() {
        table = HashBasedTable.create();
    }

    public void put(BankType bank, Collection<ExchangeRateResponse> exchangeRates) {
        exchangeRates.forEach(rate -> table.put(bank, rate.getCurrencyType(), rate));
    }

    public Collection<ExchangeRateResponse> get(BankType bank) {
        return table.row(bank).values();
    }

    public ExchangeRateResponse get(BankType bank, CurrencyType currencyType) {
        return table.get(bank, currencyType);
    }
}
