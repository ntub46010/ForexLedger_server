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

    public static Entry toRelatedBookEntry(Book relatedBook, Entry primaryBookEntry) {
        var entry = new Entry();
        entry.setBookId(relatedBook.getId());
        entry.setForeignAmount(primaryBookEntry.getRelatedBookForeignAmount());
        entry.setRelatedBookId(primaryBookEntry.getBookId());
        entry.setRelatedBookForeignAmount(primaryBookEntry.getForeignAmount());
        entry.setCreatedTime(primaryBookEntry.getCreatedTime());
        entry.setTransactionDate(primaryBookEntry.getTransactionDate());
        entry.setCreator(primaryBookEntry.getCreator());

        if (primaryBookEntry.getTransactionType() == TransactionType.TRANSFER_IN_FROM_FOREIGN) {
            entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        } else {
            entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        }

        var twdAmount = CalcUtil.divideToInt(
                CalcUtil.multiplyToDecimal(relatedBook.getRemainingTwdFund(), entry.getForeignAmount()),
                relatedBook.getBalance()
        );
        entry.setTwdAmount(twdAmount);

        return entry;
    }
}
