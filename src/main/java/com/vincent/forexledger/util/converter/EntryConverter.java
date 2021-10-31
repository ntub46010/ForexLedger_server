package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;

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

        return entry;
    }
}
