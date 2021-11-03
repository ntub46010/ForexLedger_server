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

        primaryBook.setBalance(calcPrimaryBookBalance(entry));
        relatedBook.setBalance(calcRelatedBookBalance(entry));

        primaryBook.setRemainingTwdFund(calcPrimaryBookRemainingTwdFund(entry));
        relatedBook.setRemainingTwdFund(calcRelatedBookRemainingTwdFund(entry));

        if (entry.getTransactionType().isTransferIn()) {
            var primaryBreakEvenPoint =
                    CalcUtil.divideToDouble(primaryBook.getRemainingTwdFund(), primaryBook.getBalance(), 4);
            primaryBook.setBreakEvenPoint(primaryBreakEvenPoint);
        } else {
            var relatedBreakEvenPoint =
                    CalcUtil.divideToDouble(relatedBook.getRemainingTwdFund(), relatedBook.getBalance(), 4);
            relatedBook.setBreakEvenPoint(relatedBreakEvenPoint);
        }

        if (entry.getTransactionType().isTransferIn()) {
            primaryBook.setLastForeignInvest(entry.getForeignAmount());
            primaryBook.setLastTwdInvest(entry.getTwdAmount());
        }
    }

    private double calcPrimaryBookBalance(Entry entry) {
        if (entry.getTransactionType().isTransferIn()) {
            return CalcUtil.addToDouble(primaryBook.getBalance(), entry.getForeignAmount());
        }

        if (primaryBook.getBalance() < entry.getForeignAmount()) {
            throw new InsufficientBalanceException(primaryBook.getBalance(), entry.getForeignAmount());
        }
        return CalcUtil.subtractToDouble(primaryBook.getBalance(), entry.getForeignAmount());
    }

    private double calcRelatedBookBalance(Entry entry) {
        if (!entry.getTransactionType().isTransferIn()) {
            return CalcUtil.addToDouble(relatedBook.getBalance(), entry.getForeignAmount());
        }

        if (relatedBook.getBalance() < entry.getForeignAmount()) {
            throw new InsufficientBalanceException(relatedBook.getBalance(), entry.getForeignAmount());
        }
        return CalcUtil.subtractToDouble(relatedBook.getBalance(), entry.getForeignAmount());
    }

    private int calcPrimaryBookRemainingTwdFund(Entry entry) {
        return entry.getTransactionType().isTransferIn()
                ? primaryBook.getRemainingTwdFund() + entry.getTwdAmount()
                : primaryBook.getRemainingTwdFund() - entry.getTwdAmount();
    }

    private int calcRelatedBookRemainingTwdFund(Entry entry) {
        return entry.getTransactionType().isTransferIn()
                ? relatedBook.getRemainingTwdFund() - entry.getTwdAmount()
                : relatedBook.getRemainingTwdFund() + entry.getTwdAmount();
    }

}
