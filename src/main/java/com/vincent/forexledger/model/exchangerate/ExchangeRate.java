package com.vincent.forexledger.model.exchangerate;

import com.vincent.forexledger.model.CurrencyType;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "exchange_rate")
public class ExchangeRate {
    private CurrencyType currencyType;
    private double sellingRate;
    private double buyingRate;
    private Date createTime;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
