package com.vincent.forexledger.model.bank;

public enum BankType {
    FUBON("https://www.findrate.tw/bank/8/"),
    RICHART("https://www.findrate.tw/bank/9/");

    private String exchangeRateUrl;

    BankType(String exchangeRateUrl) {
        this.exchangeRateUrl = exchangeRateUrl;
    }

    public String getExchangeRateUrl() {
        return exchangeRateUrl;
    }
}
