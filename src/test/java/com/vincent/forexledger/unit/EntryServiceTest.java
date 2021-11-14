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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntryServiceTest {

    @Test
    public void createEntryForSingleBook() {
        var userId = ObjectId.get().toString();
        var bookId = ObjectId.get().toString();
        var entryId = ObjectId.get().toString();

        var book = new Book();
        book.setId(bookId);

        var userIdentity = mock(UserIdentity.class);
        var repository = mock(EntryRepository.class);
        var bookService = mock(BookService.class);
        var entryService = new EntryService(userIdentity, repository, bookService);

        when(bookService.loadBooksByIds(List.of(bookId)))
                .thenReturn(List.of(book));
        when(userIdentity.getId()).thenReturn(userId);
        when(repository.insert(any(Entry.class)))
                .then((Answer<Entry>) invocationOnMock -> {
                    var entry = (Entry) invocationOnMock.getArgument(0);
                    entry.setId(entryId);
                    return entry;
                });

        var request = new CreateEntryRequest();
        request.setBookId(bookId);
        request.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        request.setTransactionDate(new Date());
        request.setForeignAmount(78.44);
        request.setTwdAmount(3000);
        var actualEntryId = entryService.createEntry2(request);

        var insertEntryCaptor = ArgumentCaptor.forClass(Entry.class);
        verify(repository).insert(insertEntryCaptor.capture());

        var actualEntry = insertEntryCaptor.getValue();
        Assert.assertEquals(entryId, actualEntryId);
        Assert.assertEquals(request.getBookId(), actualEntry.getBookId());
        Assert.assertEquals(request.getTransactionType(), actualEntry.getTransactionType());
        Assert.assertEquals(request.getTransactionDate(), actualEntry.getTransactionDate());
        Assert.assertEquals(request.getForeignAmount(), actualEntry.getForeignAmount(), 0);
        Assert.assertEquals(request.getTwdAmount(), actualEntry.getTwdAmount());
        Assert.assertNull(actualEntry.getRelatedBookId());
        Assert.assertNull(actualEntry.getRelatedBookForeignAmount());
        Assert.assertEquals(userId, actualEntry.getCreator());
        Assert.assertNotNull(actualEntry.getCreatedTime());

        verify(bookService).updateMetaData2(Map.of(book, actualEntry));
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
        when(bookService.loadBooksByIds(List.of(primaryBookId, relatedBookId)))
                .thenReturn(List.of(primaryBook, relatedBook));

        var request = new CreateEntryRequest();
        request.setBookId(primaryBookId);
        request.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        request.setTransactionDate(new Date());
        request.setForeignAmount(100);
        request.setRelatedBookId(relatedBookId);
        request.setRelatedBookForeignAmount(133.89);

        entryService.createEntry2(request);

        var insertEntriesCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).insert(insertEntriesCaptor.capture());

        var bookIdToEntryMap = new HashMap<String, Entry>();
        insertEntriesCaptor.getValue().forEach(e -> {
            var entry = (Entry) e;
            bookIdToEntryMap.put(entry.getBookId(), entry);
        });
        var primaryEntry = bookIdToEntryMap.get(request.getBookId());
        var relatedEntry = bookIdToEntryMap.get(request.getRelatedBookId());
        Assert.assertEquals(primaryEntry.getTransactionDate(), relatedEntry.getTransactionDate());
        Assert.assertEquals(primaryEntry.getCreatedTime(), relatedEntry.getCreatedTime());

        Assert.assertEquals(primaryBookId, primaryEntry.getBookId());
        Assert.assertEquals(TransactionType.TRANSFER_IN_FROM_FOREIGN, primaryEntry.getTransactionType());
        Assert.assertNotNull(primaryEntry.getTransactionDate());
        Assert.assertEquals(request.getForeignAmount(), primaryEntry.getForeignAmount(), 0);
        Assert.assertNotNull(primaryEntry.getTwdAmount());
        Assert.assertEquals(relatedBookId, primaryEntry.getRelatedBookId());
        Assert.assertEquals(request.getRelatedBookForeignAmount(), primaryEntry.getRelatedBookForeignAmount(), 0);
        Assert.assertEquals(userId, primaryEntry.getCreator());
        Assert.assertNotNull(primaryEntry.getCreatedTime());

        Assert.assertEquals(relatedBookId, relatedEntry.getBookId());
        Assert.assertEquals(TransactionType.TRANSFER_OUT_TO_FOREIGN, relatedEntry.getTransactionType());
        Assert.assertNotNull(relatedEntry.getTransactionDate());
        Assert.assertEquals(request.getRelatedBookForeignAmount(), relatedEntry.getForeignAmount(), 0);
        Assert.assertNotNull(relatedEntry.getTwdAmount());
        Assert.assertEquals(primaryBookId, relatedEntry.getRelatedBookId());
        Assert.assertEquals(request.getForeignAmount(), relatedEntry.getRelatedBookForeignAmount(), 0);
        Assert.assertEquals(userId, relatedEntry.getCreator());
        Assert.assertNotNull(relatedEntry.getCreatedTime());

        var expectedBookToEntryMap = new HashMap<Book, Entry>();
        expectedBookToEntryMap.put(primaryBook, primaryEntry);
        expectedBookToEntryMap.put(relatedBook,relatedEntry);
        verify(bookService).updateMetaData2(expectedBookToEntryMap);
    }

    @Test(expected = BadRequestException.class)
    public void createTransferInFromForeignEntryWithoutRelatedBookId() {
        var userIdentity = mock(UserIdentity.class);
        var repository = mock(EntryRepository.class);
        var bookService = mock(BookService.class);
        var service = new EntryService(userIdentity, repository, bookService);

        var request = new CreateEntryRequest();
        request.setBookId(ObjectId.get().toString());
        request.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        request.setTransactionDate(new Date());
        request.setForeignAmount(12487.5);
        request.setTwdAmount(null);
        request.setRelatedBookId(null);

        service.createEntry(request);
    }

    @Test(expected = BadRequestException.class)
    public void createTransferOutToForeignEntryWithoutRelatedBookId() {
        var userIdentity = mock(UserIdentity.class);
        var repository = mock(EntryRepository.class);
        var bookService = mock(BookService.class);
        var service = new EntryService(userIdentity, repository, bookService);

        var request = new CreateEntryRequest();
        request.setBookId(ObjectId.get().toString());
        request.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        request.setTransactionDate(new Date());
        request.setForeignAmount(150);
        request.setTwdAmount(null);
        request.setRelatedBookId(null);

        service.createEntry(request);
    }
}
