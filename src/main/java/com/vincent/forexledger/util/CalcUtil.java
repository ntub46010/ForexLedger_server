package com.vincent.forexledger.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalcUtil {
    private CalcUtil() {
    }

    // TODO: unit test
    public static double addToDouble(double num1, double num2) {
        return BigDecimal.valueOf(num1)
                .add(BigDecimal.valueOf(num2))
                .doubleValue();
    }

    public static double addToDouble(double num1, BigDecimal num2) {
        return BigDecimal.valueOf(num1)
                .add(num2)
                .doubleValue();
    }

    // TODO: unit test
    public static double subtractToDouble(double num1, double num2) {
        return BigDecimal.valueOf(num1)
                .subtract(BigDecimal.valueOf(num2))
                .doubleValue();
    }

    public static BigDecimal subtractToDecimal(double num1, double num2) {
        return BigDecimal.valueOf(num1)
                .subtract(BigDecimal.valueOf(num2));
    }

    public static double subtractToDouble(double num1, BigDecimal num2) {
        return BigDecimal.valueOf(num1)
                .subtract(num2)
                .doubleValue();
    }

    public static int multiplyToInt(double num1, double num2) {
        if (num1 == 0 || num2 == 0) {
            return 0;
        }

        return BigDecimal.valueOf(num1)
                .multiply(BigDecimal.valueOf(num2))
                .divide(BigDecimal.ONE, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    // TODO: unit test
    public static BigDecimal multiplyToDecimal(double num1, double num2) {
        return BigDecimal.valueOf(num1).multiply(BigDecimal.valueOf(num2));
    }

    // TODO: unit test
    public static int divideToInt(BigDecimal num1, double num2) {
        if (BigDecimal.ZERO.equals(num1)) {
            return 0;
        }

        var num2Dec = BigDecimal.valueOf(num2);
        if (num1.equals(num2Dec)) {
            return 1;
        }

        return num1
                .divide(num2Dec, RoundingMode.HALF_UP)
                .divide(BigDecimal.ONE, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    // TODO: unit test
    public static double divideToDouble(double num1, double num2, int digit) {
        if (num1 == 0) {
            return 0;
        }

        if (num1 == num2) {
            return 1;
        }

        return BigDecimal.valueOf(num1)
                .setScale(digit, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(num2), RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static double divideToDouble(int num1, double num2, int digit) {
        if (num1 == 0) {
            return 0;
        }

        return BigDecimal.valueOf(num1)
                .setScale(digit, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(num2), RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static BigDecimal divideToDecimal(BigDecimal num1, int num2, int digit) {
        if (BigDecimal.ZERO.equals(num1)) {
            return BigDecimal.ZERO;
        }

        return num1
                .setScale(digit, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(num2), RoundingMode.HALF_UP);
    }
}
