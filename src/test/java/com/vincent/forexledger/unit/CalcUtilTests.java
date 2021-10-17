package com.vincent.forexledger.unit;

import com.vincent.forexledger.util.CalcUtil;
import org.junit.Assert;
import org.junit.Test;

public class CalcUtilTests {

    @Test
    public void testMultiplyToInt() {
        Assert.assertEquals(0, CalcUtil.multiplyToInt(0, 37.8428));
        Assert.assertEquals(0, CalcUtil.multiplyToInt(621.77, 0));
        Assert.assertEquals(23529, CalcUtil.multiplyToInt(621.77, 37.8428)); // 23529.5
    }

    @Test
    public void testDivideToDouble() {
        Assert.assertEquals(0, CalcUtil.divideToDouble(0, 1, 4), 0);
        Assert.assertEquals(38.2458, CalcUtil.divideToDouble(3000, 78.44, 4), 0); // 38.24579
        Assert.assertEquals(-0.0150, CalcUtil.divideToDouble(-359, 23877, 4), 0); // -0.01503
        Assert.assertEquals(0.0194, CalcUtil.divideToDouble(1882, 97148, 4), 0); // 0.01937
    }
}
