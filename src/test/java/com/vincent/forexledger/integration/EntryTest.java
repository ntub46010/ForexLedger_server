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

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        Assert.assertEquals(38.4008, relatedBook.getBreakEvenPoint(), 0);
        Assert.assertEquals(78.44, relatedBook.getLastForeignInvest(), 0);
        Assert.assertEquals(3000, (int) relatedBook.getLastTwdInvest());
    }

    @Test
    public void testPrimaryBookTransferInButRelatedBookIsInsufficient() throws Exception {
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
        entryReq.setForeignAmount(1000);
        entryReq.setRelatedBookForeignAmount(1338.9);
        entryReq.setRelatedBookId(relatedBookId);

        mockMvc.perform(post(APIPathConstants.ENTRIES)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isUnprocessableEntity());
    }

    @SuppressWarnings({"java:S3415"})
    @Test
    public void testPrimaryBookTransferOut() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var primaryBookId = createBook("Primary Book", BankType.FUBON, CurrencyType.GBP);
        var relatedBookId = createBook("Related Book", BankType.FUBON, CurrencyType.USD);

        var primaryBook = bookRepository.findById(primaryBookId).orElseThrow();
        primaryBook.setBalance(621.77);
        primaryBook.setRemainingTwdFund(23877);
        primaryBook.setBreakEvenPoint(38.4017);
        primaryBook.setLastForeignInvest(78.44);
        primaryBook.setLastTwdInvest(3000);
        bookRepository.save(primaryBook);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(primaryBookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(133.89);
        entryReq.setRelatedBookForeignAmount(100.0);
        entryReq.setRelatedBookId(relatedBookId);
        createEntry(entryReq);

        primaryBook = bookRepository.findById(primaryBookId).orElseThrow();
        Assert.assertEquals(487.88, primaryBook.getBalance(), 0);
        Assert.assertEquals(18735, primaryBook.getRemainingTwdFund());
        Assert.assertEquals(38.4008, primaryBook.getBreakEvenPoint(), 0);
        Assert.assertEquals(78.44, primaryBook.getLastForeignInvest(), 0);
        Assert.assertEquals(3000, (int) primaryBook.getLastTwdInvest());

        var relatedBook = bookRepository.findById(relatedBookId).orElseThrow();
        Assert.assertEquals(entryReq.getRelatedBookForeignAmount(), relatedBook.getBalance(), 0);
        Assert.assertEquals(5142, relatedBook.getRemainingTwdFund());
        Assert.assertEquals(51.42, relatedBook.getBreakEvenPoint(), 0);
        Assert.assertEquals(100, relatedBook.getLastForeignInvest(), 0);
        Assert.assertEquals(5142, (int) relatedBook.getLastTwdInvest());
    }

    @Test
    public void testPrimaryBookTransferOutButBalanceIsInsufficient() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var primaryBookId = createBook("Primary Book", BankType.FUBON, CurrencyType.GBP);
        var relatedBookId = createBook("Related Book", BankType.FUBON, CurrencyType.USD);

        var primaryBook = bookRepository.findById(primaryBookId).orElseThrow();
        primaryBook.setBalance(621.77);
        primaryBook.setRemainingTwdFund(23877);
        primaryBook.setBreakEvenPoint(38.4017);
        primaryBook.setLastForeignInvest(78.44);
        primaryBook.setLastTwdInvest(3000);
        bookRepository.save(primaryBook);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(primaryBookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(1338.9);
        entryReq.setRelatedBookForeignAmount(1000.0);
        entryReq.setRelatedBookId(relatedBookId);

        mockMvc.perform(post(APIPathConstants.ENTRIES)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testLoadEntryList() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var usdBookId = createBook("My USD Book", BankType.FUBON, CurrencyType.USD);
        var gbpBookId = createBook("My GBP Book", BankType.FUBON, CurrencyType.GBP);

        var gbpEntryReq = new CreateEntryRequest();
        gbpEntryReq.setBookId(gbpBookId);
        gbpEntryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        gbpEntryReq.setTransactionDate(new Date(0));
        gbpEntryReq.setForeignAmount(150);
        gbpEntryReq.setTwdAmount(5700);
        createEntry(gbpEntryReq);

        var entryReq1 = new CreateEntryRequest();
        entryReq1.setBookId(usdBookId);
        entryReq1.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        entryReq1.setTransactionDate(new Date(1));
        entryReq1.setForeignAmount(3000);
        entryReq1.setTwdAmount(92531);
        createEntry(entryReq1);

        var entryReq2 = new CreateEntryRequest();
        entryReq2.setBookId(usdBookId);
        entryReq2.setTransactionType(TransactionType.TRANSFER_IN_FROM_INTEREST);
        entryReq2.setTransactionDate(new Date(2));
        entryReq2.setForeignAmount(27.63);
        createEntry(entryReq2);

        var entryReq3 = new CreateEntryRequest();
        entryReq3.setBookId(usdBookId);
        entryReq3.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        entryReq3.setTransactionDate(new Date(3));
        entryReq3.setForeignAmount(3027.63);
        entryReq3.setTwdAmount(93317);
        createEntry(entryReq3);

        var entryReq4 = new CreateEntryRequest();
        entryReq4.setBookId(usdBookId);
        entryReq4.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        entryReq4.setTransactionDate(new Date(4));
        entryReq4.setForeignAmount(100);
        entryReq4.setRelatedBookId(gbpBookId);
        entryReq4.setRelatedBookForeignAmount(75.02);
        createEntry(entryReq4);

        var entryReq5 = new CreateEntryRequest();
        entryReq5.setBookId(usdBookId);
        entryReq5.setTransactionType(TransactionType.TRANSFER_OUT_TO_OTHER);
        entryReq5.setTransactionDate(new Date(5));
        entryReq5.setForeignAmount(35);
        createEntry(entryReq5);

        mockMvc.perform(get(APIPathConstants.ENTRIES)
                .param("bookId", usdBookId)
                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].transactionDate").value(entryReq5.getTransactionDate().getTime()))
                .andExpect(jsonPath("$[0].transactionType").value(entryReq5.getTransactionType().name()))
                .andExpect(jsonPath("$[0].primaryAmount").value(entryReq5.getForeignAmount()))
                .andExpect(jsonPath("$[0].primaryCurrencyType").value(CurrencyType.USD.name()))

                .andExpect(jsonPath("$[1].transactionDate").value(entryReq4.getTransactionDate().getTime()))
                .andExpect(jsonPath("$[1].transactionType").value(entryReq4.getTransactionType().name()))
                .andExpect(jsonPath("$[1].primaryAmount").value(entryReq4.getForeignAmount()))
                .andExpect(jsonPath("$[1].primaryCurrencyType").value(CurrencyType.USD.name()))
                .andExpect(jsonPath("$[1].relatedAmount").value(entryReq4.getRelatedBookForeignAmount()))
                .andExpect(jsonPath("$[1].relatedCurrencyType").value(CurrencyType.GBP.name()))

                .andExpect(jsonPath("$[2].transactionDate").value(entryReq3.getTransactionDate().getTime()))
                .andExpect(jsonPath("$[2].transactionType").value(entryReq3.getTransactionType().name()))
                .andExpect(jsonPath("$[2].primaryAmount").value(entryReq3.getForeignAmount()))
                .andExpect(jsonPath("$[2].primaryCurrencyType").value(CurrencyType.USD.name()))
                .andExpect(jsonPath("$[2].relatedAmount").value(entryReq4.getTwdAmount()))

                .andExpect(jsonPath("$[3].transactionDate").value(entryReq2.getTransactionDate().getTime()))
                .andExpect(jsonPath("$[3].transactionType").value(entryReq2.getTransactionType().name()))
                .andExpect(jsonPath("$[3].primaryAmount").value(entryReq2.getForeignAmount()))
                .andExpect(jsonPath("$[3].primaryCurrencyType").value(CurrencyType.USD.name()))

                .andExpect(jsonPath("$[4].transactionDate").value(entryReq1.getTransactionDate().getTime()))
                .andExpect(jsonPath("$[4].transactionType").value(entryReq1.getTransactionType().name()))
                .andExpect(jsonPath("$[4].primaryAmount").value(entryReq1.getForeignAmount()))
                .andExpect(jsonPath("$[4].primaryCurrencyType").value(CurrencyType.USD.name()))
                .andExpect(jsonPath("$[4].relatedAmount").value(entryReq1.getTwdAmount()))



        ;
    }
}
