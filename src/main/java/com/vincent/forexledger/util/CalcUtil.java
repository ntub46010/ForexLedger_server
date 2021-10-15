package com.vincent.forexledger.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalcUtil {
    private CalcUtil() {
    }

    public static int multiplyToInt(double num1, double num2) {
        if (num1 == 0 || num2 == 0) {
            return 0;
        }

        return BigDecimal.valueOf(num1)
                .multiply(BigDecimal.valueOf(num2))
                .intValue();
    }

    public static double divideToDouble(int num1, double num2, int digit) {
        if (num1 == 0) {
            return 0;
        }

        return BigDecimal.valueOf(num1)
                .scaleByPowerOfTen(digit)
                .divide(BigDecimal.valueOf(num2), RoundingMode.HALF_DOWN)
                .scaleByPowerOfTen(-digit)
                .doubleValue();
    }
}
