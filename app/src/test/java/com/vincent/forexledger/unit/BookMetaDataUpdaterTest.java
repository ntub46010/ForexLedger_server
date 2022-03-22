package com.vincent.forexledger.unit;

import com.vincent.forexledger.exception.InsufficientBalanceException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.util.SingleBookMetaDataUpdater;
import org.junit.Assert;
import org.junit.Test;

public class BookMetaDataUpdaterTest {

    @Test
    public void testSingleBookTransferIn() {
        var book = new Book();
        var updater = new SingleBookMetaDataUpdater(book);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        entry.setForeignAmount(350);
        entry.setTwdAmount(13011);

        updater.update(entry);
        Assert.assertEquals(350, book.getBalance(), 0);
        Assert.assertEquals(13011, book.getRemainingTwdFund());
        Assert.assertEquals(37.1743, book.getBreakEvenPoint(), 0);
        Assert.assertEquals(entry.getForeignAmount(), book.getLastForeignInvest(), 0);
        Assert.assertEquals(entry.getTwdAmount(), book.getLastTwdInvest(), 0);

        entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        entry.setForeignAmount(150);
        entry.setTwdAmount(5741);

        updater.update(entry);
        Assert.assertEquals(500, book.getBalance(), 0);
        Assert.assertEquals(18752, book.getRemainingTwdFund());
        Assert.assertEquals(37.504, book.getBreakEvenPoint(), 0);
        Assert.assertEquals(entry.getForeignAmount(), book.getLastForeignInvest(), 0);
        Assert.assertEquals(entry.getTwdAmount(), book.getLastTwdInvest(), 0);
    }

    @Test
    public void testSingleBookTransferOut() {
        var book = new Book();
        book.setBalance(3027.5);
        book.setRemainingTwdFund(92531);
        book.setBreakEvenPoint(30.5635);
        book.setLastForeignInvest(3000.0);
        book.setLastTwdInvest(92531);
        var updater = new SingleBookMetaDataUpdater(book);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        entry.setForeignAmount(1027.5);
        entry.setTwdAmount(31697);

        updater.update(entry);
        Assert.assertEquals(2000, book.getBalance(), 0);
        Assert.assertEquals(60834, book.getRemainingTwdFund());
        Assert.assertEquals(30.417, book.getBreakEvenPoint(), 0);
        Assert.assertEquals(3000.0, book.getLastForeignInvest(), 0);
        Assert.assertEquals(92531, book.getLastTwdInvest(), 0);

        entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        entry.setForeignAmount(1000);
        entry.setTwdAmount(30840);

        updater.update(entry);
        Assert.assertEquals(1000, book.getBalance(), 0);
        Assert.assertEquals(29994, book.getRemainingTwdFund());
        Assert.assertEquals(29.994, book.getBreakEvenPoint(), 0);
        Assert.assertEquals(3000.0, book.getLastForeignInvest(), 0);
        Assert.assertEquals(92531, book.getLastTwdInvest(), 0);
    }

    @Test
    public void testBalanceReturnZero() {
        var book = new Book();
        book.setBalance(100);
        book.setRemainingTwdFund(2785);
        book.setBreakEvenPoint(27.85);
        book.setLastForeignInvest(100.0);
        book.setLastTwdInvest(2785);
        var updater = new SingleBookMetaDataUpdater(book);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        entry.setForeignAmount(100);
        entry.setTwdAmount(2775);

        updater.update(entry);
        Assert.assertEquals(0, book.getBalance(), 0);
        Assert.assertEquals(10, book.getRemainingTwdFund());
        Assert.assertNull(book.getBreakEvenPoint());
        Assert.assertEquals(100.0, book.getLastForeignInvest(), 0);
        Assert.assertEquals(2785, book.getLastTwdInvest(), 0);
    }

    @Test
    public void testTransferInFromInterest() {
        var book = new Book();
        book.setBalance(2500);
        book.setRemainingTwdFund(5504);
        book.setBreakEvenPoint(2.2016);
        book.setLastForeignInvest(2500.0);
        book.setLastTwdInvest(5504);
        var updater = new SingleBookMetaDataUpdater(book);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_INTEREST);
        entry.setForeignAmount(0.27);

        updater.update(entry);
        Assert.assertEquals(2500.27, book.getBalance(), 0);
        Assert.assertEquals(5504, book.getRemainingTwdFund());
        Assert.assertEquals(2.2014, book.getBreakEvenPoint(), 0);
        Assert.assertEquals(2500, book.getLastForeignInvest(), 0);
        Assert.assertEquals(5504, book.getLastTwdInvest(), 0);
    }

    @Test
    public void testRecoverAllTwdFund() {
        var book = new Book();
        book.setBalance(100);
        book.setRemainingTwdFund(2785);
        book.setBreakEvenPoint(27.85);
        book.setLastForeignInvest(100.0);
        book.setLastTwdInvest(2785);
        var updater = new SingleBookMetaDataUpdater(book);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        entry.setForeignAmount(95);
        entry.setTwdAmount(2945);

        updater.update(entry);
        Assert.assertEquals(5, book.getBalance(), 0);
        Assert.assertEquals(0, book.getRemainingTwdFund());
        Assert.assertEquals(0, book.getBreakEvenPoint(), 0);
        Assert.assertEquals(100.0, book.getLastForeignInvest(), 0);
        Assert.assertEquals(2785, book.getLastTwdInvest(), 0);
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testSingleBookTransferOutButBalanceIsInsufficient() {
        var book = new Book();
        book.setBalance(621.77);
        book.setRemainingTwdFund(23877);
        book.setBreakEvenPoint(38.4017);
        book.setLastForeignInvest(78.44);
        book.setLastTwdInvest(3000);
        var updater = new SingleBookMetaDataUpdater(book);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        entry.setForeignAmount(700);
        entry.setTwdAmount(26149);

        updater.update(entry);
    }

}
