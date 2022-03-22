package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.util.converter.EntryConverter;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

    @Test
    public void testConvertToRelatedEntry() {
        var primaryEntry = new Entry();
        primaryEntry.setBookId(ObjectId.get().toString());
        primaryEntry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        primaryEntry.setTransactionDate(new Date());
        primaryEntry.setForeignAmount(100);
        primaryEntry.setRelatedBookId(ObjectId.get().toString());
        primaryEntry.setRelatedBookForeignAmount(75.02);
        primaryEntry.setCreator(ObjectId.get().toString());
        primaryEntry.setCreatedTime(new Date());

        var relatedEntry = EntryConverter.toRelatedEntry(primaryEntry);
        assertEquals(primaryEntry.getRelatedBookId(), relatedEntry.getBookId());
        assertEquals(TransactionType.TRANSFER_OUT_TO_FOREIGN, relatedEntry.getTransactionType());
        assertEquals(primaryEntry.getTransactionDate(), relatedEntry.getTransactionDate());
        assertEquals(primaryEntry.getRelatedBookForeignAmount(), relatedEntry.getForeignAmount(), 0);
        assertNull(relatedEntry.getTwdAmount());
        assertEquals(primaryEntry.getBookId(), relatedEntry.getRelatedBookId());
        assertEquals(primaryEntry.getForeignAmount(), relatedEntry.getRelatedBookForeignAmount(), 0);
        assertEquals(primaryEntry.getCreator(), relatedEntry.getCreator());
        assertEquals(primaryEntry.getCreatedTime(), relatedEntry.getCreatedTime());
    }

}
