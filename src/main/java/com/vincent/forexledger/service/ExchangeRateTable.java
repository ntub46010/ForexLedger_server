package com.vincent.forexledger.service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRateResponse;
import org.springframework.data.util.Pair;

import java.util.*;

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

    public Map<BankType, Map<CurrencyType, Double>> getBuyingRates(
            Collection<Pair<BankType, CurrencyType>> bankCurrencyTypePairs) {
        var result = new EnumMap<BankType, Map<CurrencyType, Double>>(BankType.class);

        bankCurrencyTypePairs.forEach(paris -> {
            var bank = paris.getFirst();
            var currencyType = paris.getSecond();

            var currencyTypeToRateMap = result.get(bank);
            if (currencyTypeToRateMap == null) {
                currencyTypeToRateMap = new EnumMap<>(CurrencyType.class);
            }

            var rate = table.get(bank, currencyType);
            Objects.requireNonNull(rate);
            currencyTypeToRateMap.put(currencyType, rate.getBuyingRate());
        });

        return result;
    }
}
