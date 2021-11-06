package com.vincent.forexledger.unit;

import com.vincent.forexledger.exception.InsufficientBalanceException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.util.DoubleBookMetaDataUpdater;
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

    @Test
    public void testPrimaryBookTransferIn() {
        var primaryBook = new Book();
        var relatedBook = new Book();
        relatedBook.setBalance(621.77);
        relatedBook.setRemainingTwdFund(23877);
        relatedBook.setBreakEvenPoint(38.4017);
        relatedBook.setLastForeignInvest(78.44);
        relatedBook.setLastTwdInvest(3000);

        var updater = new DoubleBookMetaDataUpdater(primaryBook, relatedBook);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        entry.setForeignAmount(100);
        entry.setRelatedForeignAmount(133.89);

        updater.update(entry);

        Assert.assertEquals(100, primaryBook.getBalance(), 0);
        Assert.assertEquals(5141, primaryBook.getRemainingTwdFund());
        Assert.assertEquals(51.41, primaryBook.getBreakEvenPoint(), 0);
        Assert.assertEquals(100, primaryBook.getLastForeignInvest(), 0);
        Assert.assertEquals(5141, primaryBook.getLastTwdInvest(), 0);

        Assert.assertEquals(487.88, relatedBook.getBalance(), 0);
        Assert.assertEquals(18736, relatedBook.getRemainingTwdFund());
        Assert.assertEquals(38.4017, relatedBook.getBreakEvenPoint(), 0);
        Assert.assertEquals(78.44, relatedBook.getLastForeignInvest(), 0);
        Assert.assertEquals(3000, relatedBook.getLastTwdInvest(), 0);
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testPrimaryBookTransferInButRelatedBookIsInsufficient() {
        var primaryBook = new Book();
        var relatedBook = new Book();
        var updater = new DoubleBookMetaDataUpdater(primaryBook, relatedBook);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        entry.setForeignAmount(100);
        entry.setRelatedForeignAmount(133.89);

        updater.update(entry);
    }

    @Test
    public void testPrimaryBookTransferOut() {
        var primaryBook = new Book();
        primaryBook.setBalance(621.77);
        primaryBook.setRemainingTwdFund(23877);
        primaryBook.setBreakEvenPoint(38.4017);
        primaryBook.setLastForeignInvest(78.44);
        primaryBook.setLastTwdInvest(3000);

        var relatedBook = new Book();
        var updater = new DoubleBookMetaDataUpdater(primaryBook, relatedBook);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        entry.setForeignAmount(133.89);
        entry.setRelatedForeignAmount(100.0);

        updater.update(entry);

        Assert.assertEquals(487.88, primaryBook.getBalance(), 0);
        Assert.assertEquals(18736, primaryBook.getRemainingTwdFund());
        Assert.assertEquals(38.4017, primaryBook.getBreakEvenPoint(), 0);
        Assert.assertEquals(78.44, primaryBook.getLastForeignInvest(), 0);
        Assert.assertEquals(3000, primaryBook.getLastTwdInvest(), 0);

        Assert.assertEquals(100, relatedBook.getBalance(), 0);
        Assert.assertEquals(5141, relatedBook.getRemainingTwdFund());
        Assert.assertEquals(51.41, relatedBook.getBreakEvenPoint(), 0);
        Assert.assertEquals(100, relatedBook.getLastForeignInvest(), 0);
        Assert.assertEquals(5141, relatedBook.getLastTwdInvest(), 0);
    }

    @Test
    public void testPrimaryBookTransferOutButBalanceIsInsufficient() {
        var primaryBook = new Book();
        primaryBook.setBalance(621.77);
        primaryBook.setRemainingTwdFund(23877);
        primaryBook.setBreakEvenPoint(38.4017);
        primaryBook.setLastForeignInvest(78.44);
        primaryBook.setLastTwdInvest(3000);

        var relatedBook = new Book();
        var updater = new DoubleBookMetaDataUpdater(primaryBook, relatedBook);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        entry.setForeignAmount(267.78);
        entry.setRelatedForeignAmount(200.0);

        updater.update(entry);
    }
}
