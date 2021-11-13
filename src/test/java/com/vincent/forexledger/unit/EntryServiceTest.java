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
import java.util.List;
import java.util.Map;

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
