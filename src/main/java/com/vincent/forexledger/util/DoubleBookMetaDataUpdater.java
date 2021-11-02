package com.vincent.forexledger.util;

import com.vincent.forexledger.exception.InsufficientBalanceException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;

public class DoubleBookMetaDataUpdater {
    private Book primaryBook;
    private Book relatedBook;

    public DoubleBookMetaDataUpdater(Book primaryBook, Book relatedBook) {
        this.primaryBook = primaryBook;
        this.relatedBook = relatedBook;
    }


    public void update(Entry entry) {
        if (entry.getTransactionType() != TransactionType.TRANSFER_IN_FROM_FOREIGN &&
                entry.getTransactionType() != TransactionType.TRANSFER_OUT_TO_FOREIGN) {
            throw new IllegalArgumentException("Invalid transaction type: " + entry.getTransactionType().name());
        }
        //
    }

    private double calcPrimaryBookBalance(Entry entry) {
        if (entry.getTransactionType() == TransactionType.TRANSFER_IN_FROM_FOREIGN) {
            return CalcUtil.addToDouble(primaryBook.getBalance(), entry.getForeignAmount());
        } else {
            if (primaryBook.getBalance() < entry.getForeignAmount()) {
                throw new InsufficientBalanceException(primaryBook.getBalance(), entry.getForeignAmount());
            }
            return CalcUtil.subtractToDouble(primaryBook.getBalance(), entry.getForeignAmount());
        }
    }

    private double calcRelatedBookBalance(Entry entry) {
        if (entry.getTransactionType() == TransactionType.TRANSFER_IN_FROM_FOREIGN) {
            if (relatedBook.getBalance() < entry.getForeignAmount()) {
                throw new InsufficientBalanceException(relatedBook.getBalance(), entry.getForeignAmount());
            }
            return CalcUtil.subtractToDouble(relatedBook.getBalance(), entry.getForeignAmount());
        } else {
            return CalcUtil.addToDouble(relatedBook.getBalance(), entry.getForeignAmount());
        }
    }

}
