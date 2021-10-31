package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.entry.CreateEntryRequest;
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

        var entry = EntryConverter.toEntry(request);

        Assert.assertEquals(request.getBookId(), entry.getBookId());
        Assert.assertEquals(request.getTransactionType(), entry.getTransactionType());
        Assert.assertEquals(request.getTransactionDate(), entry.getTransactionDate());
        Assert.assertEquals(request.getForeignAmount(), entry.getForeignAmount(), 0);
        Assert.assertEquals(request.getTwdAmount(), entry.getTwdAmount());
        Assert.assertEquals(request.getRelatedBookId(), entry.getRelatedBookId());
    }
}
