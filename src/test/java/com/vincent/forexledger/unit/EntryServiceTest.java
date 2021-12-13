package com.vincent.forexledger.unit;

import com.vincent.forexledger.exception.BadRequestException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.service.BookService;
import com.vincent.forexledger.service.EntryService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntryServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    public void createEntryForSingleBook() {
        var userId = ObjectId.get().toString();
        var bookId = ObjectId.get().toString();

        var book = new Book();
        book.setId(bookId);

        var userIdentity = mock(UserIdentity.class);
        var repository = mock(EntryRepository.class);
        var bookService = mock(BookService.class);
        var entryService = new EntryService(userIdentity, repository, bookService);

        when(bookService.loadBooksByIds(Set.of(bookId)))
                .thenReturn(List.of(book));
        when(userIdentity.getId()).thenReturn(userId);

        var request = new CreateEntryRequest();
        request.setBookId(bookId);
        request.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        request.setTransactionDate(new Date());
        request.setForeignAmount(78.44);
        request.setTwdAmount(3000);
        entryService.createEntry(request);

        var insertEntryCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(repository).insert(insertEntryCaptor.capture());

        var actualEntry = (Entry) insertEntryCaptor.getValue().iterator().next();
        assertEquals(request.getBookId(), actualEntry.getBookId());
        assertEquals(request.getTransactionType(), actualEntry.getTransactionType());
        assertEquals(request.getTransactionDate(), actualEntry.getTransactionDate());
        assertEquals(request.getForeignAmount(), actualEntry.getForeignAmount(), 0);
        assertEquals(request.getTwdAmount(), actualEntry.getTwdAmount());
        assertNull(actualEntry.getRelatedBookId());
        assertNull(actualEntry.getRelatedBookForeignAmount());
        assertEquals(userId, actualEntry.getCreator());
        assertNotNull(actualEntry.getCreatedTime());

        verify(bookService).updateMetaData(Map.of(book, actualEntry));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createEntriesForDoubleBooks() {
        var userIdentity = mock(UserIdentity.class);
        var repository = mock(EntryRepository.class);
        var bookService = mock(BookService.class);
        var entryService = new EntryService(userIdentity, repository, bookService);

        var userId = ObjectId.get().toString();
        var primaryBookId = ObjectId.get().toString();
        var relatedBookId = ObjectId.get().toString();

        var primaryBook = new Book();
        primaryBook.setId(primaryBookId);
        var relatedBook = new Book();
        relatedBook.setId(relatedBookId);
        relatedBook.setBalance(621.77);
        relatedBook.setRemainingTwdFund(23877);
        relatedBook.setBreakEvenPoint(38.4017);
        relatedBook.setLastForeignInvest(78.44);
        relatedBook.setLastTwdInvest(3000);

        when(userIdentity.getId()).thenReturn(userId);
        when(bookService.loadBooksByIds(Set.of(primaryBookId, relatedBookId)))
                .thenReturn(List.of(primaryBook, relatedBook));

        var request = new CreateEntryRequest();
        request.setBookId(primaryBookId);
        request.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        request.setTransactionDate(new Date());
        request.setForeignAmount(100);
        request.setRelatedBookId(relatedBookId);
        request.setRelatedBookForeignAmount(75.02);

        entryService.createEntry(request);

        var insertEntriesCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(repository).insert(insertEntriesCaptor.capture());

        var bookIdToEntryMap = new HashMap<String, Entry>();
        insertEntriesCaptor.getValue().forEach(e -> {
            var entry = (Entry) e;
            bookIdToEntryMap.put(entry.getBookId(), entry);
        });
        var primaryEntry = bookIdToEntryMap.get(request.getBookId());
        var relatedEntry = bookIdToEntryMap.get(request.getRelatedBookId());
        assertEquals(primaryEntry.getTransactionDate(), relatedEntry.getTransactionDate());
        assertEquals(primaryEntry.getCreatedTime(), relatedEntry.getCreatedTime());

        assertEquals(primaryBookId, primaryEntry.getBookId());
        assertEquals(TransactionType.TRANSFER_IN_FROM_FOREIGN, primaryEntry.getTransactionType());
        assertNotNull(primaryEntry.getTransactionDate());
        assertEquals(request.getForeignAmount(), primaryEntry.getForeignAmount(), 0);
        assertNull(primaryEntry.getTwdAmount());
        assertEquals(relatedBookId, primaryEntry.getRelatedBookId());
        assertEquals(request.getRelatedBookForeignAmount(), primaryEntry.getRelatedBookForeignAmount(), 0);
        assertEquals(userId, primaryEntry.getCreator());
        assertNotNull(primaryEntry.getCreatedTime());

        assertEquals(relatedBookId, relatedEntry.getBookId());
        assertEquals(TransactionType.TRANSFER_OUT_TO_FOREIGN, relatedEntry.getTransactionType());
        assertNotNull(relatedEntry.getTransactionDate());
        assertEquals(request.getRelatedBookForeignAmount(), relatedEntry.getForeignAmount(), 0);
        assertNull(relatedEntry.getTwdAmount());
        assertEquals(primaryBookId, relatedEntry.getRelatedBookId());
        assertEquals(request.getForeignAmount(), relatedEntry.getRelatedBookForeignAmount(), 0);
        assertEquals(userId, relatedEntry.getCreator());
        assertNotNull(relatedEntry.getCreatedTime());

        var expectedBookToEntryMap = new HashMap<Book, Entry>();
        expectedBookToEntryMap.put(primaryBook, primaryEntry);
        expectedBookToEntryMap.put(relatedBook, relatedEntry);
        verify(bookService).updateMetaData(expectedBookToEntryMap);
    }

    @Test(expected = BadRequestException.class)
    public void createTransferInFromForeignBookWithoutRelatedBookId() {
        var userIdentity = mock(UserIdentity.class);
        var repository = mock(EntryRepository.class);
        var bookService = mock(BookService.class);
        var service = new EntryService(userIdentity, repository, bookService);

        var request = new CreateEntryRequest();
        request.setBookId(ObjectId.get().toString());
        request.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        request.setTransactionDate(new Date());
        request.setForeignAmount(100);
        request.setRelatedBookForeignAmount(133.89);

        service.createEntry(request);
    }

    @Test(expected = BadRequestException.class)
    public void createTransferOutToForeignBookWithoutRelatedForeignAmount() {
        var userIdentity = mock(UserIdentity.class);
        var repository = mock(EntryRepository.class);
        var bookService = mock(BookService.class);
        var service = new EntryService(userIdentity, repository, bookService);

        var request = new CreateEntryRequest();
        request.setBookId(ObjectId.get().toString());
        request.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        request.setTransactionDate(new Date());
        request.setForeignAmount(133.89);
        request.setRelatedBookForeignAmount(100.0);

        service.createEntry(request);
    }
}
