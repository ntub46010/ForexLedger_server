package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.service.BookService;
import com.vincent.forexledger.service.ExchangeRateTable;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    @Test
    public void testCreateBook() {
        var userId = ObjectId.get().toString();
        var bookId = ObjectId.get().toString();

        var userIdentity = mock(UserIdentity.class);
        var repository = mock(BookRepository.class);
        var service = new BookService(userIdentity, repository, null);

        when(userIdentity.getId()).thenReturn(userId);
        when(repository.insert(any(Book.class)))
                .then((Answer<Book>) invocationOnMock -> {
                    var book = (Book) invocationOnMock.getArgument(0);
                    book.setId(bookId);
                    return book;
                });

        var request = new CreateBookRequest();
        request.setName("Book Name");
        request.setBank(BankType.RICHART);
        request.setCurrencyType(CurrencyType.JPY);
        var createdBookId = service.createBook(request);

        verify(userIdentity).getId();

        var insertBookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(repository).insert(insertBookCaptor.capture());

        var actualBook = insertBookCaptor.getValue();
        Assert.assertNotNull(createdBookId);
        Assert.assertEquals(request.getName(), actualBook.getName());
        Assert.assertEquals(request.getBank(), actualBook.getBank());
        Assert.assertEquals(request.getCurrencyType(), actualBook.getCurrencyType());
        Assert.assertEquals(userId, actualBook.getCreator());
        Assert.assertNotNull(actualBook.getCreatedTime());
        Assert.assertEquals(0, actualBook.getBalance(), 0);
        Assert.assertEquals(0, actualBook.getRemainingTwdFund());
        Assert.assertNull(actualBook.getBreakEvenPoint());
        Assert.assertNull(actualBook.getLastForeignInvest());
        Assert.assertNull(actualBook.getLastTwdInvest());
    }

    @Test
    public void testLoadMyBooks() {
        var userId1 = ObjectId.get().toString();
        var userId2 = ObjectId.get().toString();

        var books1 = List.of(
                createEmptyBook(userId1, BankType.FUBON, CurrencyType.USD),
                createEmptyBook(userId1, BankType.FUBON, CurrencyType.GBP));
        var books2 = List.of(
                createEmptyBook(userId2, BankType.RICHART, CurrencyType.USD),
                createEmptyBook(userId2, BankType.RICHART, CurrencyType.GBP));
        var buyingRateMap = Map.of(
                BankType.FUBON, Map.of(CurrencyType.USD, 27.8655, CurrencyType.GBP, 38.2206),
                BankType.RICHART, Map.of(CurrencyType.USD, 27.89, CurrencyType.GBP, 38.464));

        var userIdentity = mock(UserIdentity.class);
        var repository = mock(BookRepository.class);
        var exchangeTable = mock(ExchangeRateTable.class);
        var service = new BookService(userIdentity, repository, exchangeTable);

        when(userIdentity.getId()).thenReturn(userId1);
        when(repository.findByCreator(userId1)).thenReturn(books1);
        when(repository.findByCreator(userId2)).thenReturn(books2);
        when(exchangeTable.getBuyingRates(anyCollection())).thenReturn(buyingRateMap);

        var responses = service.loadMyBooks();

        verify(userIdentity).getId();
        verify(repository).findByCreator(userId1);
        verify(repository, times(0)).findByCreator(userId2);
        verify(exchangeTable).getBuyingRates(anyCollection());

        var bookIds1 = books1.stream()
                .map(Book::getId)
                .collect(Collectors.toList());
        var responseIds = responses.stream()
                .map(BookListResponse::getId)
                .collect(Collectors.toList());
        assertTrue(CollectionUtils.isEqualCollection(bookIds1, responseIds));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateSingleBookMetaDataByTwdEntry() {
        var book = new Book();
        book.setId(ObjectId.get().toString());

        var entry = new Entry();
        entry.setBookId(ObjectId.get().toString());
        entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        entry.setForeignAmount(350);
        entry.setTwdAmount(13011);

        var repository = mock(BookRepository.class);
        var service = new BookService(null, repository, null);
        service.updateMetaData(Collections.singletonMap(book, entry));

        var booksCaptor = ArgumentCaptor.forClass(Set.class);
        verify(repository).saveAll(booksCaptor.capture());

        assertTrue(booksCaptor.getValue().contains(book));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateDoubleBookMetaDataByForeignEntry() {
        var repository = mock(BookRepository.class);
        var service = new BookService(null, repository, null);

        var primaryBook = new Book();
        primaryBook.setId(ObjectId.get().toString());
        primaryBook.setBalance(621.77);
        primaryBook.setRemainingTwdFund(23877);

        var relatedBook = new Book();
        relatedBook.setId(ObjectId.get().toString());

        var primaryEntry = new Entry();
        primaryEntry.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        primaryEntry.setForeignAmount(75.02);

        var relatedEntry = new Entry();
        relatedEntry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        relatedEntry.setForeignAmount(100);

        var bookToEntryMap = new HashMap<Book, Entry>();
        bookToEntryMap.put(primaryBook, primaryEntry);
        bookToEntryMap.put(relatedBook, relatedEntry);
        service.updateMetaData(bookToEntryMap);

        var saveBookCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(repository).saveAll(saveBookCaptor.capture());

        var actualBookMap = new HashMap<String, Book>();
        saveBookCaptor.getValue().forEach(elem -> {
            var book = (Book) elem;
            actualBookMap.put(book.getId(), book);
        });

        primaryBook = actualBookMap.get(primaryBook.getId());
        assertEquals(546.75, primaryBook.getBalance(), 0);
        assertEquals(20996, primaryBook.getRemainingTwdFund());
        assertEquals(38.4015, primaryBook.getBreakEvenPoint(), 0);

        relatedBook = actualBookMap.get(relatedBook.getId());
        assertEquals(100, relatedBook.getBalance(), 0);
        assertEquals(2881, relatedBook.getRemainingTwdFund());
        assertEquals(28.81, relatedBook.getBreakEvenPoint(), 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateSingleBookMetaDataWhenTransferOutToOther() {
        var repository = mock(BookRepository.class);
        var service = new BookService(null, repository, null);

        var book = new Book();
        book.setBalance(3000);
        book.setRemainingTwdFund(92531);

        var entry = new Entry();
        entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_OTHER);
        entry.setForeignAmount(2000);

        var bookToEntryMap = Map.of(book, entry);
        service.updateMetaData(bookToEntryMap);

        var saveBookCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(repository).saveAll(saveBookCaptor.capture());

        book = (Book) saveBookCaptor.getValue().iterator().next();
        assertEquals(1000, book.getBalance(), 0);
        assertEquals(30844, book.getRemainingTwdFund());
        assertEquals(30.844, book.getBreakEvenPoint(), 0);
    }

    private Book createEmptyBook(String creator, BankType bank, CurrencyType currencyType) {
        var book = new Book();
        book.setId(ObjectId.get().toString());
        book.setName("Book Name");
        book.setBank(bank);
        book.setCurrencyType(currencyType);
        book.setBalance(0);
        book.setRemainingTwdFund(0);
        book.setCreator(creator);

        return book;
    }
}
