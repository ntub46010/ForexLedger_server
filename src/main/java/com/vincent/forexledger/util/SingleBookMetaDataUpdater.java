package com.vincent.forexledger.util;

import com.vincent.forexledger.exception.InsufficientBalanceException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.Entry;

public class SingleBookMetaDataUpdater {
    private Book book;

    public SingleBookMetaDataUpdater(Book book) {
        this.book = book;
    }

    public void update(Entry entry) {
        //
    }

    private void updateBalance(Entry entry) {
        var currentBalance = book.getBalance();
        if (entry.getTransactionType().isTransferIn()) {
            var balance = CalcUtil.addToDouble(currentBalance, entry.getForeignAmount());
            book.setBalance(balance);
        } else {
            if (currentBalance > entry.getForeignAmount()) {
                throw new InsufficientBalanceException(currentBalance, entry.getForeignAmount());
            }

            var balance = CalcUtil.subtractToDouble(currentBalance, entry.getForeignAmount());
            book.setBalance(balance);
        }
    }
}
