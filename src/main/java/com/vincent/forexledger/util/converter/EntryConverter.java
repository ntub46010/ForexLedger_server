package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.EntryListResponse;
import com.vincent.forexledger.model.entry.TransactionType;

import java.util.Optional;

public class EntryConverter {
    private EntryConverter() {
    }

    public static Entry toEntry(CreateEntryRequest request) {
        var entry = new Entry();
        entry.setBookId(request.getBookId());
        entry.setTransactionType(request.getTransactionType());
        entry.setTransactionDate(request.getTransactionDate());
        entry.setForeignAmount(request.getForeignAmount());
        entry.setTwdAmount(request.getTwdAmount());
        entry.setRelatedBookId(request.getRelatedBookId());
        entry.setRelatedBookForeignAmount(request.getRelatedBookForeignAmount());

        return entry;
    }

    public static Entry toRelatedEntry(Entry primaryEntry) {
        var entry = new Entry();
        if (primaryEntry.getTransactionType() == TransactionType.TRANSFER_IN_FROM_FOREIGN) {
            entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        } else if (primaryEntry.getTransactionType() == TransactionType.TRANSFER_OUT_TO_FOREIGN) {
            entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        } else {
            throw new IllegalArgumentException("Transaction type of primary entry is unexpected.");
        }

        entry.setBookId(primaryEntry.getRelatedBookId());
        entry.setTransactionDate(primaryEntry.getTransactionDate());
        entry.setForeignAmount(primaryEntry.getRelatedBookForeignAmount());
        entry.setRelatedBookId(primaryEntry.getBookId());
        entry.setRelatedBookForeignAmount(primaryEntry.getForeignAmount());
        entry.setCreator(primaryEntry.getCreator());
        entry.setCreatedTime(primaryEntry.getCreatedTime());

        return entry;
    }

    // TODO: unit test
    public static EntryListResponse toEntryListResponse(Entry entry) {
        var response = new EntryListResponse();
        response.setId(entry.getId());
        response.setTransactionDate(entry.getTransactionDate());
        response.setTransactionType(entry.getTransactionType());
        response.setPrimaryAmount(entry.getForeignAmount());
        response.setDescription(entry.getDescription());

        if (entry.getTransactionType().isRelatedToTwd()) {
            response.setRelatedAmount(entry.getTwdAmount().doubleValue());
        } else if (entry.getTransactionType().canRelateBook()) {
            // if entry didn't related to another book, then TWD cost should be provided when creating entry
            var relatedAmount = Optional.ofNullable(entry.getRelatedBookForeignAmount())
                    .orElse(entry.getTwdAmount().doubleValue());
            response.setRelatedAmount(relatedAmount);
        }

        return response;
    }
}
