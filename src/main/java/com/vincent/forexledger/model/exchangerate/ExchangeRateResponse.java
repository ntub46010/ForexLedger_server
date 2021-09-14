package com.vincent.forexledger.model.exchangerate;

import com.vincent.forexledger.model.CurrencyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public class ExchangeRateResponse {
    @Schema(example = "USD")
    private CurrencyType currencyType;

    @Schema(description = "The exchange rate that bank selling currency to user.")
    private double sellingRate;

    @Schema(description = "The exchange rate that bank buying currency from user.")
    private double buyingRate;

    @Schema(description = "The refreshing time at server of this exchange rate.")
    private Date updatedTime;

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

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTimeime) {
        this.updatedTime = updatedTimeime;
    }
}
