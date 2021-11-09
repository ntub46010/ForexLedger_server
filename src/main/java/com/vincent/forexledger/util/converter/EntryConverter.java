package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.util.CalcUtil;

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

    // TODO: unit test
    public static Entry toRelatedBookEntry(String relatedBookId, double relatedBookBalance, Entry primaryBookEntry) {
        var entry = new Entry();
        entry.setBookId(relatedBookId);
        entry.setForeignAmount(primaryBookEntry.getRelatedBookForeignAmount());
        entry.setRelatedBookId(primaryBookEntry.getBookId());
        entry.setRelatedBookForeignAmount(primaryBookEntry.getForeignAmount());

        if (primaryBookEntry.getTransactionType() == TransactionType.TRANSFER_IN_FROM_FOREIGN) {
            entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        } else {
            entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        }

        var twdAmount = CalcUtil.divideToInt(
                CalcUtil.multiplyToDecimal(relatedBookBalance, entry.getForeignAmount()),
                relatedBookBalance
        );
        entry.setTwdAmount(twdAmount);

        return entry;
    }
}
