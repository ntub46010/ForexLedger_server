package com.vincent.forexledger.unit;

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
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntryServiceTest {

    @Test
    public void createEntry() {
        var userId = ObjectId.get().toString();
        var entryId = ObjectId.get().toString();

        var userIdentity = mock(UserIdentity.class);
        var repository = mock(EntryRepository.class);
        var bookService = mock(BookService.class);
        var service = new EntryService(userIdentity, repository, bookService);

        when(userIdentity.getId()).thenReturn(userId);
        when(repository.insert(any(Entry.class)))
                .then((Answer<Entry>) invocationOnMock -> {
                    var entry = (Entry) invocationOnMock.getArgument(0);
                    entry.setId(entryId);
                    return entry;
                });

        var request = new CreateEntryRequest();
        request.setBookId(ObjectId.get().toString());
        request.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        request.setTransactionDate(new Date());
        request.setForeignAmount(78.44);
        request.setTwdAmount(3000);
        request.setAnotherBookId(ObjectId.get().toString());
        var actualEntryId = service.createEntry(request);

        verify(userIdentity).getId();

        var insertEntryCaptor = ArgumentCaptor.forClass(Entry.class);
        verify(repository).insert(insertEntryCaptor.capture());

        Assert.assertEquals(entryId, actualEntryId);

        var actualEntry = insertEntryCaptor.getValue();
        Assert.assertEquals(request.getBookId(), actualEntry.getBookId());
        Assert.assertEquals(request.getTransactionType(), actualEntry.getTransactionType());
        Assert.assertEquals(request.getTransactionDate(), actualEntry.getTransactionDate());
        Assert.assertEquals(request.getForeignAmount(), actualEntry.getForeignAmount(), 0);
        Assert.assertEquals(request.getTwdAmount(), actualEntry.getTwdAmount());
        Assert.assertEquals(request.getAnotherBookId(), actualEntry.getAnotherBookId());
        Assert.assertEquals(userId, actualEntry.getCreator());
        Assert.assertNotNull(actualEntry.getCreatedTime());
    }
}
