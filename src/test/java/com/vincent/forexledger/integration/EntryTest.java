package com.vincent.forexledger.integration;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.TransactionType;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class EntryTest extends BaseTest {

    @Test
    public void testBookTransferIn() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var bookId = createBook("Test Book", BankType.FUBON, CurrencyType.GBP);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(bookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(150);
        entryReq.setTwdAmount(5751);

        var mvcResult = mockMvc.perform(post(APIPathConstants.ENTRIES)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isCreated())
                .andReturn();
        var location = mvcResult.getResponse().getHeader(HttpHeaders.LOCATION);
        var entryId = StringUtils.substringAfterLast(location, '/');

        var entry = entryRepository.findById(entryId).orElseThrow();
        Assert.assertEquals(entry.getBookId(), entryReq.getBookId());
        Assert.assertEquals(entry.getTransactionType(), entryReq.getTransactionType());
        Assert.assertEquals(entry.getTransactionDate(), entryReq.getTransactionDate());
        Assert.assertEquals(entry.getForeignAmount(), entryReq.getForeignAmount(), 0);
        Assert.assertEquals(entry.getTwdAmount(), entryReq.getTwdAmount());
        Assert.assertNull(entry.getRelatedBookId());
        Assert.assertNull(entry.getRelatedBookForeignAmount());

        var book = bookRepository.findById(bookId).orElseThrow();
        Assert.assertEquals(entryReq.getForeignAmount(), book.getBalance(), 0);
        Assert.assertEquals((int) entryReq.getTwdAmount(), book.getRemainingTwdFund());
        Assert.assertEquals(38.34, book.getBreakEvenPoint(), 0);
        Assert.assertEquals(entryReq.getForeignAmount(), book.getLastForeignInvest(), 0);
        Assert.assertEquals(entryReq.getTwdAmount(), book.getLastTwdInvest());
    }

    @SuppressWarnings({"java:S3415"})
    @Test
    public void testBookTransferOut() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var bookId = createBook("Test Book", BankType.FUBON, CurrencyType.CHF);

        var book = bookRepository.findById(bookId).orElseThrow();
        book.setBalance(1500);
        book.setRemainingTwdFund(46851);
        book.setBreakEvenPoint(31.234);
        book.setLastForeignInvest(500.0);
        book.setLastTwdInvest(15580);
        bookRepository.save(book);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(bookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(900);
        entryReq.setTwdAmount(28111);
        createEntry(entryReq);

        book = bookRepository.findById(bookId).orElseThrow();
        Assert.assertEquals(600, book.getBalance(), 0);
        Assert.assertEquals(18740, book.getRemainingTwdFund());
        Assert.assertEquals(31.234, book.getBreakEvenPoint(), 0.001);
        Assert.assertEquals(500, book.getLastForeignInvest(), 0);
        Assert.assertEquals(15580, (int) book.getLastTwdInvest());
    }

    @Test
    public void testBookTransferOutButBalanceIsInsufficient() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var bookId = createBook("Test Book", BankType.FUBON, CurrencyType.CHF);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(bookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(900);
        entryReq.setTwdAmount(28111);

        mockMvc.perform(post(APIPathConstants.ENTRIES)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isUnprocessableEntity());
    }

    @SuppressWarnings({"java:S3415"})
    @Test
    public void testPrimaryBookTransferIn() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var primaryBookId = createBook("Primary Book", BankType.FUBON, CurrencyType.USD);
        var relatedBookId = createBook("Related Book", BankType.FUBON, CurrencyType.GBP);

        var relatedBook = bookRepository.findById(relatedBookId).orElseThrow();
        relatedBook.setBalance(621.77);
        relatedBook.setRemainingTwdFund(23877);
        relatedBook.setBreakEvenPoint(38.4017);
        relatedBook.setLastForeignInvest(78.44);
        relatedBook.setLastTwdInvest(3000);
        bookRepository.save(relatedBook);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(primaryBookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(100);
        entryReq.setRelatedBookForeignAmount(133.89);
        entryReq.setRelatedBookId(relatedBookId);
        createEntry(entryReq);

        var primaryBook = bookRepository.findById(primaryBookId).orElseThrow();
        Assert.assertEquals(entryReq.getForeignAmount(), primaryBook.getBalance(), 0);
        Assert.assertEquals(5142, primaryBook.getRemainingTwdFund());
        Assert.assertEquals(51.42, primaryBook.getBreakEvenPoint(), 0);
        Assert.assertEquals(100, primaryBook.getLastForeignInvest(), 0);
        Assert.assertEquals(5142, (int) primaryBook.getLastTwdInvest());

        relatedBook = bookRepository.findById(relatedBookId).orElseThrow();
        Assert.assertEquals(487.88, relatedBook.getBalance(), 0);
        Assert.assertEquals(18735, relatedBook.getRemainingTwdFund());
        Assert.assertEquals(38.4017, relatedBook.getBreakEvenPoint(), 0);
        Assert.assertEquals(78.44, relatedBook.getLastForeignInvest(), 0);
        Assert.assertEquals(3000, (int) relatedBook.getLastTwdInvest());
    }
}
