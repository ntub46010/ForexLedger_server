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

    @Deprecated
    public static Entry toRelatedBookEntry(Book transferOutBook, Entry primaryBookEntry) {
        var entry = new Entry();
        entry.setBookId(primaryBookEntry.getRelatedBookId());
        entry.setForeignAmount(primaryBookEntry.getRelatedBookForeignAmount());
        entry.setRelatedBookId(primaryBookEntry.getBookId());
        entry.setRelatedBookForeignAmount(primaryBookEntry.getForeignAmount());
        entry.setCreatedTime(primaryBookEntry.getCreatedTime());
        entry.setTransactionDate(primaryBookEntry.getTransactionDate());
        entry.setCreator(primaryBookEntry.getCreator());

        double transferOutForeignAmount;
        if (primaryBookEntry.getTransactionType() == TransactionType.TRANSFER_IN_FROM_FOREIGN) {
            entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
            transferOutForeignAmount = primaryBookEntry.getRelatedBookForeignAmount();
        } else {
            entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
            transferOutForeignAmount = primaryBookEntry.getForeignAmount();
        }

        var twdAmount = CalcUtil.divideToInt(
                CalcUtil.multiplyToDecimal(transferOutBook.getRemainingTwdFund(), transferOutForeignAmount),
                transferOutBook.getBalance()
        );
        entry.setTwdAmount(twdAmount);

        return entry;
    }

    // TODO: unit test
    public static Entry toRelatedEntry2(Entry primaryEntry) {
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
}
