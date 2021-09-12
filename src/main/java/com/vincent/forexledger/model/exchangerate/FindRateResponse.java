package com.vincent.forexledger.model.exchangerate;

import com.vincent.forexledger.model.CurrencyType;

public class FindRateResponse {
    private CurrencyType currencyType;
    private double sellingRate;
    private double buyingRate;

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public double getSellingRate() {
        return sellingRate;
    }

    public void setSellingRate(double sellingRate) {
        this.sellingRate = sellingRate;
    }

    public double getBuyingRate() {
        return buyingRate;
    }

    public void setBuyingRate(double buyingRate) {
        this.buyingRate = buyingRate;
    }
}
