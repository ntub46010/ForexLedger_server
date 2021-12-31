package com.vincent.forexledger.util;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public class ExchangeRateDiscountTable {
    private static Map<BankType, Map<CurrencyType, BigDecimal>> table;

    private ExchangeRateDiscountTable() {
    }

    static {
        var fubonDiscountMap = new EnumMap<CurrencyType, BigDecimal>(CurrencyType.class);
        fubonDiscountMap.put(CurrencyType.USD, BigDecimal.valueOf(0.03));
        fubonDiscountMap.put(CurrencyType.EUR, BigDecimal.valueOf(0.03));

        var richartDiscountMap = new EnumMap<CurrencyType, BigDecimal>(CurrencyType.class);
        richartDiscountMap.put(CurrencyType.USD, BigDecimal.valueOf(0.03));
        richartDiscountMap.put(CurrencyType.CNY, BigDecimal.valueOf(0.004));
        richartDiscountMap.put(CurrencyType.JPY, BigDecimal.valueOf(0.0008));
        richartDiscountMap.put(CurrencyType.EUR, BigDecimal.valueOf(0.08));
        richartDiscountMap.put(CurrencyType.HKD, BigDecimal.valueOf(0.01));
        richartDiscountMap.put(CurrencyType.AUD, BigDecimal.valueOf(0.044));
        richartDiscountMap.put(CurrencyType.GBP, BigDecimal.valueOf(0.09));

        table = new EnumMap<>(BankType.class);
        table.put(BankType.FUBON, fubonDiscountMap);
        table.put(BankType.RICHART, richartDiscountMap);
    }

    public static void updatedToDiscountedRate(FindRateResponse rate) {
        var sellingRate = rate.getSellingRate();
        var buyingRate = rate.getBuyingRate();
        var discount = getDiscount(rate.getBankType(), rate.getCurrencyType());

        if (BigDecimal.ZERO.equals(discount) && rate.getBankType() == BankType.RICHART) {
            discount = CalcUtil.divideToDecimal(
                    CalcUtil.subtractToDecimal(sellingRate, buyingRate), 5, 4);
        }

        sellingRate = CalcUtil.subtractToDouble(sellingRate, discount);
        buyingRate = CalcUtil.addToDouble(buyingRate, discount);
        rate.setSellingRate(sellingRate);
        rate.setBuyingRate(buyingRate);
    }

    private static BigDecimal getDiscount(BankType bank, CurrencyType currency) {
        return table.getOrDefault(bank, Map.of())
                .getOrDefault(currency, BigDecimal.ZERO);
    }
}
