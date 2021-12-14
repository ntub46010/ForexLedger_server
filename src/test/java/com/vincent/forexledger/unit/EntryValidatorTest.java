package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.validation.*;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntryValidatorTest {

    @Test
    public void testTransferInFromTwd() {
        var validator = EntryValidatorFactory
                .getCreateEntryValidator(TransactionType.TRANSFER_IN_FROM_TWD);
        assertEquals(TwdEntryValidator.class, validator.getClass());

        var request1 = new CreateEntryRequest();
        request1.setBookId(ObjectId.get().toString());
        request1.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        request1.setTransactionDate(new Date());
        request1.setForeignAmount(100);
        request1.setTwdAmount(2800);
        assertTrue(validator.validate(request1));

        var request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setTwdAmount(null);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setTwdAmount(-2800);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookId(ObjectId.get().toString());
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookForeignAmount(75.02);
        assertFalse(validator.validate(request2));
    }

    @Test
    public void testTransferOutToTwd() {
        var validator = EntryValidatorFactory
                .getCreateEntryValidator(TransactionType.TRANSFER_OUT_TO_TWD);
        assertEquals(TwdEntryValidator.class, validator.getClass());

        var request1 = new CreateEntryRequest();
        request1.setBookId(ObjectId.get().toString());
        request1.setTransactionType(TransactionType.TRANSFER_OUT_TO_TWD);
        request1.setTransactionDate(new Date());
        request1.setForeignAmount(100);
        request1.setTwdAmount(2900);
        assertTrue(validator.validate(request1));

        var request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setTwdAmount(null);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setTwdAmount(-2900);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookId(ObjectId.get().toString());
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookForeignAmount(133.89);
        assertFalse(validator.validate(request2));
    }

    @Test
    public void testTransferInFromForeign() {
        var validator = EntryValidatorFactory
                .getCreateEntryValidator(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        assertEquals(ForeignTransferInEntryValidator.class, validator.getClass());

        var baseRequest = new CreateEntryRequest();
        baseRequest.setBookId(ObjectId.get().toString());
        baseRequest.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        baseRequest.setTransactionDate(new Date());
        baseRequest.setForeignAmount(514);

        var request = new CreateEntryRequest();
        BeanUtils.copyProperties(baseRequest, request);
        request.setTwdAmount(16637);
        assertTrue(validator.validate(request));

        request = new CreateEntryRequest();
        BeanUtils.copyProperties(baseRequest, request);
        request.setRelatedBookId(ObjectId.get().toString());
        request.setRelatedBookForeignAmount(5000.0);
        assertTrue(validator.validate(request));

        request = new CreateEntryRequest();
        BeanUtils.copyProperties(baseRequest, request);
        request.setRelatedBookId(ObjectId.get().toString());
        assertFalse(validator.validate(request));

        request = new CreateEntryRequest();
        BeanUtils.copyProperties(baseRequest, request);
        request.setRelatedBookForeignAmount(5000.0);
        assertFalse(validator.validate(request));

        request = new CreateEntryRequest();
        BeanUtils.copyProperties(baseRequest, request);
        request.setTwdAmount(16637);
        request.setRelatedBookId(ObjectId.get().toString());
        request.setRelatedBookForeignAmount(5000.0);
        assertFalse(validator.validate(request));

        request = new CreateEntryRequest();
        BeanUtils.copyProperties(baseRequest, request);
        request.setTwdAmount(-16637);
        assertFalse(validator.validate(request));

        request = new CreateEntryRequest();
        BeanUtils.copyProperties(baseRequest, request);
        request.setRelatedBookId(ObjectId.get().toString());
        request.setRelatedBookForeignAmount(-5000.0);
        assertFalse(validator.validate(request));
    }

    @Test
    public void testTransferOutToForeign() {
        var validator = EntryValidatorFactory
                .getCreateEntryValidator(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        assertEquals(ForeignTransferOutEntryValidator.class, validator.getClass());

        var request1 = new CreateEntryRequest();
        request1.setBookId(ObjectId.get().toString());
        request1.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        request1.setTransactionDate(new Date());
        request1.setForeignAmount(5000);
        assertTrue(validator.validate(request1));

        var request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookId(ObjectId.get().toString());
        request2.setRelatedBookForeignAmount(514.0);
        assertTrue(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setTwdAmount(16637);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookId(ObjectId.get().toString());
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookForeignAmount(514.0);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setTwdAmount(16637);
        request2.setRelatedBookId(ObjectId.get().toString());
        request2.setRelatedBookForeignAmount(514.0);
        assertFalse(validator.validate(request2));
    }

    @Test
    public void testTransferInFromInterest() {
        var validator = EntryValidatorFactory
                .getCreateEntryValidator(TransactionType.TRANSFER_IN_FROM_INTEREST);
        assertEquals(InterestEntryValidator.class, validator.getClass());

        var request1 = new CreateEntryRequest();
        request1.setBookId(ObjectId.get().toString());
        request1.setTransactionType(TransactionType.TRANSFER_IN_FROM_INTEREST);
        request1.setTransactionDate(new Date());
        request1.setForeignAmount(126.93);
        assertTrue(validator.validate(request1));

        var request2 = new CreateEntryRequest();
        request2.setTwdAmount(192);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        request2.setRelatedBookId(ObjectId.get().toString());
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        request2.setRelatedBookForeignAmount(1.0);
        assertFalse(validator.validate(request2));
    }

    @Test
    public void testTransferInFromOther() {
        var validator = EntryValidatorFactory
                .getCreateEntryValidator(TransactionType.TRANSFER_IN_FROM_OTHER);
        assertEquals(OtherTransferInEntryValidator.class, validator.getClass());

        var request1 = new CreateEntryRequest();
        request1.setBookId(ObjectId.get().toString());
        request1.setTransactionType(TransactionType.TRANSFER_IN_FROM_OTHER);
        request1.setTransactionDate(new Date());
        request1.setForeignAmount(2100);
        request1.setTwdAmount(0);
        assertTrue(validator.validate(request1));

        request1 = new CreateEntryRequest();
        request1.setTwdAmount(56000);
        assertTrue(validator.validate(request1));

        var request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setTwdAmount(null);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setTwdAmount(-56000);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookId(ObjectId.get().toString());
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookForeignAmount(0.0);
        assertFalse(validator.validate(request2));
    }

    @Test
    public void testTransferOutToOther() {
        var validator = EntryValidatorFactory
                .getCreateEntryValidator(TransactionType.TRANSFER_OUT_TO_OTHER);
        assertEquals(OtherTransferOutEntryValidator.class, validator.getClass());

        var request1 = new CreateEntryRequest();
        request1.setBookId(ObjectId.get().toString());
        request1.setTransactionType(TransactionType.TRANSFER_OUT_TO_OTHER);
        request1.setTransactionDate(new Date());
        request1.setForeignAmount(2000);
        assertTrue(validator.validate(request1));

        var request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setTwdAmount(56000);
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookId(ObjectId.get().toString());
        assertFalse(validator.validate(request2));

        request2 = new CreateEntryRequest();
        BeanUtils.copyProperties(request1, request2);
        request2.setRelatedBookForeignAmount(1.0);
        assertFalse(validator.validate(request2));
    }
}
