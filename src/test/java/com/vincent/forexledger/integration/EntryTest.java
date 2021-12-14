package com.vincent.forexledger.integration;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
        var entryId = createEntry(entryReq);

        var entry = entryRepository.findById(entryId).orElseThrow();
        assertEquals(entryReq.getBookId(), entry.getBookId());
        assertEquals(entryReq.getTransactionType(), entry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), entry.getTransactionDate());
        assertEquals(entryReq.getForeignAmount(), entry.getForeignAmount(), 0);
        assertEquals(entryReq.getTwdAmount(), entry.getTwdAmount());
        assertNull(entry.getRelatedBookId());
        assertNull(entry.getRelatedBookForeignAmount());

        var book = bookRepository.findById(bookId).orElseThrow();
        assertEquals(entryReq.getForeignAmount(), book.getBalance(), 0);
        assertEquals((int) entryReq.getTwdAmount(), book.getRemainingTwdFund());
        assertEquals(38.34, book.getBreakEvenPoint(), 0);
        assertEquals(entryReq.getForeignAmount(), book.getLastForeignInvest(), 0);
        assertEquals(entryReq.getTwdAmount(), book.getLastTwdInvest());
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
        var entryId = createEntry(entryReq);

        var entry = entryRepository.findById(entryId).orElseThrow();
        assertEquals(entryReq.getBookId(), entry.getBookId());
        assertEquals(entryReq.getTransactionType(), entry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), entry.getTransactionDate());
        assertEquals(entryReq.getForeignAmount(), entry.getForeignAmount(), 0);
        assertEquals(entryReq.getTwdAmount(), entry.getTwdAmount());
        assertNull(entry.getRelatedBookId());
        assertNull(entry.getRelatedBookForeignAmount());

        book = bookRepository.findById(bookId).orElseThrow();
        assertEquals(600, book.getBalance(), 0);
        assertEquals(18740, book.getRemainingTwdFund());
        assertEquals(31.234, book.getBreakEvenPoint(), 0.001);
        assertEquals(500, book.getLastForeignInvest(), 0);
        assertEquals(15580, (int) book.getLastTwdInvest());
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
        entryReq.setRelatedBookId(relatedBookId);
        entryReq.setRelatedBookForeignAmount(75.02);
        createEntry(entryReq);

        var bookEntryMap = entryRepository.findAll().stream()
                .collect(Collectors.toMap(Entry::getBookId, Function.identity()));
        assertEquals(2, bookEntryMap.size());

        var primaryEntry = bookEntryMap.get(primaryBookId);
        assertEquals(entryReq.getBookId(), primaryEntry.getBookId());
        assertEquals(entryReq.getTransactionType(), primaryEntry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), primaryEntry.getTransactionDate());
        assertEquals(entryReq.getForeignAmount(), primaryEntry.getForeignAmount(), 0);
        assertNull(primaryEntry.getTwdAmount());
        assertEquals(relatedBookId, primaryEntry.getRelatedBookId());
        assertEquals(entryReq.getRelatedBookForeignAmount(), primaryEntry.getRelatedBookForeignAmount());

        var relatedEntry = bookEntryMap.get(relatedBookId);
        assertEquals(relatedBookId, relatedEntry.getBookId());
        assertEquals(TransactionType.TRANSFER_OUT_TO_FOREIGN, relatedEntry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), relatedEntry.getTransactionDate());
        assertEquals(entryReq.getRelatedBookForeignAmount(), relatedEntry.getForeignAmount(), 0);
        assertNull(relatedEntry.getTwdAmount());
        assertEquals(primaryBookId, relatedEntry.getRelatedBookId());
        assertEquals(entryReq.getForeignAmount(), relatedEntry.getRelatedBookForeignAmount(), 0);

        var primaryBook = bookRepository.findById(primaryBookId).orElseThrow();
        assertEquals(entryReq.getForeignAmount(), primaryBook.getBalance(), 0);
        assertEquals(2881, primaryBook.getRemainingTwdFund());
        assertEquals(28.81, primaryBook.getBreakEvenPoint(), 0);
        assertEquals(100, primaryBook.getLastForeignInvest(), 0);
        assertEquals(2881, (int) primaryBook.getLastTwdInvest());

        relatedBook = bookRepository.findById(relatedBookId).orElseThrow();
        assertEquals(546.75, relatedBook.getBalance(), 0);
        assertEquals(20996, relatedBook.getRemainingTwdFund());
        assertEquals(38.4015, relatedBook.getBreakEvenPoint(), 0);
        assertEquals(78.44, relatedBook.getLastForeignInvest(), 0);
        assertEquals(3000, (int) relatedBook.getLastTwdInvest());
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
        entryReq.setRelatedBookForeignAmount(750.2);
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
        entryReq.setForeignAmount(75.02);
        entryReq.setRelatedBookId(relatedBookId);
        entryReq.setRelatedBookForeignAmount(100.0);
        createEntry(entryReq);

        var bookEntryMap = entryRepository.findAll().stream()
                .collect(Collectors.toMap(Entry::getBookId, Function.identity()));
        assertEquals(2, bookEntryMap.size());

        var primaryEntry = bookEntryMap.get(primaryBookId);
        assertEquals(entryReq.getBookId(), primaryEntry.getBookId());
        assertEquals(entryReq.getTransactionType(), primaryEntry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), primaryEntry.getTransactionDate());
        assertEquals(entryReq.getForeignAmount(), primaryEntry.getForeignAmount(), 0);
        assertNull(primaryEntry.getTwdAmount());
        assertEquals(relatedBookId, primaryEntry.getRelatedBookId());
        assertEquals(entryReq.getRelatedBookForeignAmount(), primaryEntry.getRelatedBookForeignAmount());

        var relatedEntry = bookEntryMap.get(relatedBookId);
        assertEquals(relatedBookId, relatedEntry.getBookId());
        assertEquals(TransactionType.TRANSFER_IN_FROM_FOREIGN, relatedEntry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), relatedEntry.getTransactionDate());
        assertEquals(entryReq.getRelatedBookForeignAmount(), relatedEntry.getForeignAmount(), 0);
        assertNull(relatedEntry.getTwdAmount());
        assertEquals(primaryBookId, relatedEntry.getRelatedBookId());
        assertEquals(entryReq.getForeignAmount(), relatedEntry.getRelatedBookForeignAmount(), 0);

        primaryBook = bookRepository.findById(primaryBookId).orElseThrow();
        assertEquals(546.75, primaryBook.getBalance(), 0);
        assertEquals(20996, primaryBook.getRemainingTwdFund());
        assertEquals(38.4015, primaryBook.getBreakEvenPoint(), 0);
        assertEquals(78.44, primaryBook.getLastForeignInvest(), 0);
        assertEquals(3000, (int) primaryBook.getLastTwdInvest());

        var relatedBook = bookRepository.findById(relatedBookId).orElseThrow();
        assertEquals(entryReq.getRelatedBookForeignAmount(), relatedBook.getBalance(), 0);
        assertEquals(2881, relatedBook.getRemainingTwdFund());
        assertEquals(28.81, relatedBook.getBreakEvenPoint(), 0);
        assertEquals(100, relatedBook.getLastForeignInvest(), 0);
        assertEquals(2881, (int) relatedBook.getLastTwdInvest());
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
        entryReq.setForeignAmount(750.2);
        entryReq.setRelatedBookForeignAmount(1000.0);
        entryReq.setRelatedBookId(relatedBookId);

        mockMvc.perform(post(APIPathConstants.ENTRIES)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isUnprocessableEntity());
    }

    @SuppressWarnings({"java:S3415"})
    @Test
    public void testTransferInFromForeignWithoutRelation() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var bookId = createBook("My Book", BankType.FUBON, CurrencyType.USD);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(bookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(514);
        entryReq.setTwdAmount(16637);

        var entryId = createEntry(entryReq);
        var entry = entryRepository.findById(entryId).orElseThrow();
        assertEquals(entryReq.getBookId(), entry.getBookId());
        assertEquals(entryReq.getTransactionType(), entry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), entry.getTransactionDate());
        assertEquals(entryReq.getForeignAmount(), entry.getForeignAmount(), 0);
        assertEquals(entryReq.getTwdAmount(), entry.getTwdAmount());
        assertNull(entry.getRelatedBookId());
        assertNull(entry.getRelatedBookForeignAmount());

        var book = bookRepository.findById(bookId).orElseThrow();
        assertEquals(entryReq.getForeignAmount(), book.getBalance(), 0);
        assertEquals((int) entryReq.getTwdAmount(), book.getRemainingTwdFund());
        assertEquals(32.3677, book.getBreakEvenPoint(), 0);
        assertEquals(entryReq.getForeignAmount(), book.getLastForeignInvest(), 0);
        assertEquals((int) entryReq.getTwdAmount(), (int) book.getLastTwdInvest());
    }

    @SuppressWarnings({"java:S3415"})
    @Test
    public void testTransferOutToForeignWithoutRelation() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var bookId = createBook("My Book", BankType.FUBON, CurrencyType.GBP);

        var book = bookRepository.findById(bookId).orElseThrow();
        book.setBalance(621.77);
        book.setRemainingTwdFund(23877);
        book.setBreakEvenPoint(38.4017);
        book.setLastForeignInvest(78.44);
        book.setLastTwdInvest(3000);
        bookRepository.save(book);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(bookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(75.02);

        var entryId = createEntry(entryReq);
        var entry = entryRepository.findById(entryId).orElseThrow();
        assertEquals(entryReq.getBookId(), entry.getBookId());
        assertEquals(entryReq.getTransactionType(), entry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), entry.getTransactionDate());
        assertEquals(entryReq.getForeignAmount(), entry.getForeignAmount(), 0);
        assertEquals(entryReq.getTwdAmount(), entry.getTwdAmount());
        assertNull(entry.getRelatedBookId());
        assertNull(entry.getRelatedBookForeignAmount());

        var resultBook = bookRepository.findById(bookId).orElseThrow();
        assertEquals(546.75, resultBook.getBalance(), 0);
        assertEquals(20996, resultBook.getRemainingTwdFund());
        assertEquals(38.4015, resultBook.getBreakEvenPoint(), 0);
        assertEquals(book.getLastForeignInvest(), resultBook.getLastForeignInvest(), 0);
        assertEquals((int) book.getLastTwdInvest(), (int) resultBook.getLastTwdInvest());
    }

    @SuppressWarnings({"java:S3415"})
    @Test
    public void testTransferInFromInterest() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var bookId = createBook("My Book", BankType.FUBON, CurrencyType.USD);

        var book = bookRepository.findById(bookId).orElseThrow();
        book.setBalance(3000);
        book.setRemainingTwdFund(92531);
        book.setBreakEvenPoint(30.8437);
        book.setLastForeignInvest(3000.0);
        book.setLastTwdInvest(92531);
        bookRepository.save(book);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(bookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_INTEREST);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(27.5);

        var entryId = createEntry(entryReq);
        var entry = entryRepository.findById(entryId).orElseThrow();
        assertEquals(entryReq.getBookId(), entry.getBookId());
        assertEquals(entryReq.getTransactionType(), entry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), entry.getTransactionDate());
        assertEquals(entryReq.getForeignAmount(), entry.getForeignAmount(), 0);
        assertNull(entry.getTwdAmount());
        assertNull(entry.getRelatedBookId());
        assertNull(entry.getRelatedBookForeignAmount());

        var resultBook = bookRepository.findById(bookId).orElseThrow();
        assertEquals(3027.5, resultBook.getBalance(), 0);
        assertEquals(book.getRemainingTwdFund(), resultBook.getRemainingTwdFund());
        assertEquals(30.5635, resultBook.getBreakEvenPoint(), 0);
        assertEquals(book.getLastForeignInvest(), resultBook.getLastForeignInvest(), 0);
        assertEquals((int) book.getLastTwdInvest(), (int) resultBook.getLastTwdInvest());
    }

    @SuppressWarnings({"java:S3415"})
    @Test
    public void testTransferOutToOther() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var bookId = createBook("My Book", BankType.FUBON, CurrencyType.USD);

        var book = bookRepository.findById(bookId).orElseThrow();
        book.setBalance(3000);
        book.setRemainingTwdFund(92531);
        book.setBreakEvenPoint(30.8437);
        book.setLastForeignInvest(3000.0);
        book.setLastTwdInvest(92531);
        bookRepository.save(book);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(bookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_OUT_TO_OTHER);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(2000);

        var entryId = createEntry(entryReq);
        var entry = entryRepository.findById(entryId).orElseThrow();
        assertEquals(entryReq.getBookId(), entry.getBookId());
        assertEquals(entryReq.getTransactionType(), entry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), entry.getTransactionDate());
        assertEquals(entryReq.getForeignAmount(), entry.getForeignAmount(), 0);
        assertNull(entry.getTwdAmount());
        assertNull(entry.getRelatedBookId());
        assertNull(entry.getRelatedBookForeignAmount());

        var resultBook = bookRepository.findById(bookId).orElseThrow();
        assertEquals(1000, resultBook.getBalance(), 0);
        assertEquals(30844, resultBook.getRemainingTwdFund());
        assertEquals(30.844, resultBook.getBreakEvenPoint(), 0);
        assertEquals(book.getLastForeignInvest(), resultBook.getLastForeignInvest(), 0);
        assertEquals((int) book.getLastTwdInvest(), (int) resultBook.getLastTwdInvest());
    }

    @Test
    public void testTransferInFromOther() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var bookId = createBook("My Book", BankType.FUBON, CurrencyType.USD);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(bookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_OTHER);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(2100);
        entryReq.setTwdAmount(61687);

        var entryId = createEntry(entryReq);
        var entry = entryRepository.findById(entryId).orElseThrow();
        assertEquals(entryReq.getBookId(), entry.getBookId());
        assertEquals(entryReq.getTransactionType(), entry.getTransactionType());
        assertEquals(entryReq.getTransactionDate(), entry.getTransactionDate());
        assertEquals(entryReq.getForeignAmount(), entry.getForeignAmount(), 0);
        assertEquals(entryReq.getTwdAmount(), entry.getTwdAmount());
        assertNull(entry.getRelatedBookId());
        assertNull(entry.getRelatedBookForeignAmount());

        var resultBook = bookRepository.findById(bookId).orElseThrow();
        assertEquals(entryReq.getForeignAmount(), resultBook.getBalance(), 0);
        assertEquals((int) entryReq.getTwdAmount(), resultBook.getRemainingTwdFund());
        assertEquals(29.3748, resultBook.getBreakEvenPoint(), 0);
    }
}
