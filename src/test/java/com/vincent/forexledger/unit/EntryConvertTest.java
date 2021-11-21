package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.util.converter.EntryConverter;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class EntryConvertTest {

    @Test
    public void testConvertCreatingEntry() {
        var request = new CreateEntryRequest();
        request.setBookId(ObjectId.get().toString());
        request.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        request.setTransactionDate(new Date());
        request.setForeignAmount(78.44);
        request.setTwdAmount(3000);
        request.setRelatedBookId(ObjectId.get().toString());
        request.setRelatedBookForeignAmount(0.0);

        var entry = EntryConverter.toEntry(request);

        Assert.assertEquals(request.getBookId(), entry.getBookId());
        Assert.assertEquals(request.getTransactionType(), entry.getTransactionType());
        Assert.assertEquals(request.getTransactionDate(), entry.getTransactionDate());
        Assert.assertEquals(request.getForeignAmount(), entry.getForeignAmount(), 0);
        Assert.assertEquals(request.getTwdAmount(), entry.getTwdAmount());
        Assert.assertEquals(request.getRelatedBookId(), entry.getRelatedBookId());
        Assert.assertEquals(request.getRelatedBookForeignAmount(), entry.getRelatedBookForeignAmount());
    }

    @SuppressWarnings({"java:S3415"})
    @Test
    public void testConvertToRelatedBookEntry() {
        var relatedBook = new Book();
        relatedBook.setId(ObjectId.get().toString());
        relatedBook.setBalance(621.77);
        relatedBook.setRemainingTwdFund(23877);

        var primaryBookEntry = new Entry();
        primaryBookEntry.setBookId(ObjectId.get().toString());
        primaryBookEntry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        primaryBookEntry.setTransactionDate(new Date(1));
        primaryBookEntry.setForeignAmount(100);
        primaryBookEntry.setRelatedBookId(relatedBook.getId());
        primaryBookEntry.setRelatedBookForeignAmount(133.89);
        primaryBookEntry.setCreator(ObjectId.get().toString());
        primaryBookEntry.setCreatedTime(new Date(2));

        var relatedBookEntry = EntryConverter
                .toRelatedBookEntry(relatedBook, primaryBookEntry);

        Assert.assertEquals(relatedBook.getId(), relatedBookEntry.getBookId());
        Assert.assertEquals(TransactionType.TRANSFER_OUT_TO_FOREIGN, relatedBookEntry.getTransactionType());
        Assert.assertEquals(primaryBookEntry.getTransactionDate(), relatedBookEntry.getTransactionDate());
        Assert.assertEquals(primaryBookEntry.getRelatedBookForeignAmount(), relatedBookEntry.getForeignAmount(), 0);
        Assert.assertEquals(primaryBookEntry.getBookId(), relatedBookEntry.getRelatedBookId());
        Assert.assertEquals(primaryBookEntry.getForeignAmount(), relatedBookEntry.getRelatedBookForeignAmount(), 0);
        Assert.assertEquals(5142, relatedBookEntry.getTwdAmount(), 0);
        Assert.assertEquals(primaryBookEntry.getCreator(), relatedBookEntry.getCreator());
        Assert.assertEquals(primaryBookEntry.getCreatedTime(), relatedBookEntry.getCreatedTime());
    }
}
