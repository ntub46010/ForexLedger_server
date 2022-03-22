package com.vincent.forexledger.unit;

import com.vincent.forexledger.util.CalcUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class CalcUtilTests {

    @Test
    public void testAddToDouble() {
        Assert.assertEquals(27.9985, CalcUtil.addToDouble(27.9685, BigDecimal.valueOf(0.03)), 0);
    }

    @Test
    public void testSubtractToDouble() {
        Assert.assertEquals(28.0385, CalcUtil.subtractToDouble(28.0685, BigDecimal.valueOf(0.03)), 0);
    }

    @Test
    public void testSubtractToDecimal() {
        Assert.assertEquals(27.9705, CalcUtil.subtractToDecimal(28.0005, 0.03).doubleValue(), 0);
    }

    @Test
    public void testMultiplyToInt() {
        Assert.assertEquals(0, CalcUtil.multiplyToInt(0, 37.8428));
        Assert.assertEquals(0, CalcUtil.multiplyToInt(621.77, 0));
        Assert.assertEquals(23530, CalcUtil.multiplyToInt(621.77, 37.8428)); // 23529.5
    }

    @Test
    public void testDivideToDouble() {
        Assert.assertEquals(0, CalcUtil.divideToDouble(0, 1, 4), 0);
        Assert.assertEquals(38.2458, CalcUtil.divideToDouble(3000, 78.44, 4), 0); // 38.24579
        Assert.assertEquals(-0.0150, CalcUtil.divideToDouble(-359, 23877, 4), 0); // -0.01503
        Assert.assertEquals(0.0194, CalcUtil.divideToDouble(1882, 97148, 4), 0); // 0.01937
    }

    @Test
    public void testDivideToDecimal() {
        var result = CalcUtil.divideToDecimal(BigDecimal.valueOf(0), 5, 4).doubleValue();
        Assert.assertEquals(0.0, result, 0);

        result = CalcUtil.divideToDecimal(BigDecimal.valueOf(0.48), 5, 4).doubleValue();
        Assert.assertEquals(0.096, result, 0);
    }
}
