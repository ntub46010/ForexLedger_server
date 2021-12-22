package com.vincent.forexledger.integration;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.service.RefreshBookMetaDataTask;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class RefreshBookMetaDataTest extends BaseTest {

    @Autowired
    private RefreshBookMetaDataTask task;

    @SuppressWarnings({"java:S3415"})
    @Test
    public void test() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var usdBookId = createBook("My USD Book", BankType.FUBON, CurrencyType.USD);
        var gbpBookId = createBook("My GBP Book", BankType.FUBON, CurrencyType.GBP);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(gbpBookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        entryReq.setTransactionDate(new Date(0));
        entryReq.setForeignAmount(150);
        entryReq.setTwdAmount(5700);
        createEntry(entryReq);

        // entry 1
        entryReq = new CreateEntryRequest();
        entryReq.setBookId(usdBookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        entryReq.setTransactionDate(new Date(1));
        entryReq.setForeignAmount(3000);
        entryReq.setTwdAmount(92531);
        createEntry(entryReq);

        task.process();
        var actualBook = bookRepository.findById(usdBookId).orElseThrow();
        assertEquals(3000, actualBook.getBalance(), 0);
        assertEquals(92531, actualBook.getRemainingTwdFund());
        assertEquals(30.8437, actualBook.getBreakEvenPoint(), 0);
        assertEquals(3000, actualBook.getLastForeignInvest(), 0);
        assertEquals(92531, actualBook.getLastTwdInvest(), 0);

        // entry 2
        entryReq = new CreateEntryRequest();
        entryReq.setBookId(usdBookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_INTEREST);
        entryReq.setTransactionDate(new Date(2));
        entryReq.setForeignAmount(27.63);
        createEntry(entryReq);

        task.process();
        actualBook = bookRepository.findById(usdBookId).orElseThrow();
        assertEquals(3027.63, actualBook.getBalance(), 0);
        assertEquals(92531, actualBook.getRemainingTwdFund());
        assertEquals(30.5622, actualBook.getBreakEvenPoint(), 0);
        assertEquals(3000, actualBook.getLastForeignInvest(), 0);
        assertEquals(92531, actualBook.getLastTwdInvest(), 0);

        // entry 3
        entryReq = new CreateEntryRequest();
        entryReq.setBookId(usdBookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        entryReq.setTransactionDate(new Date(3));
        entryReq.setForeignAmount(3000);
        entryReq.setTwdAmount(92465);
        createEntry(entryReq);

        task.process();
        actualBook = bookRepository.findById(usdBookId).orElseThrow();
        assertEquals(27.63, actualBook.getBalance(), 0);
        assertEquals(66, actualBook.getRemainingTwdFund());
        assertEquals(2.3887, actualBook.getBreakEvenPoint(), 0);
        assertEquals(3000, actualBook.getLastForeignInvest(), 0);
        assertEquals(92531, actualBook.getLastTwdInvest(), 0);

        // entry 4
        entryReq = new CreateEntryRequest();
        entryReq.setBookId(usdBookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        entryReq.setTransactionDate(new Date(4));
        entryReq.setForeignAmount(100);
        entryReq.setRelatedBookId(gbpBookId);
        entryReq.setRelatedBookForeignAmount(75.02);
        createEntry(entryReq);

        task.process();
        actualBook = bookRepository.findById(usdBookId).orElseThrow();
        assertEquals(127.63, actualBook.getBalance(), 0);
        assertEquals(2917, actualBook.getRemainingTwdFund());
        assertEquals(22.8551, actualBook.getBreakEvenPoint(), 0);
        assertEquals(100, actualBook.getLastForeignInvest(), 0);
        assertEquals(2851, actualBook.getLastTwdInvest(), 0);
    }
}
