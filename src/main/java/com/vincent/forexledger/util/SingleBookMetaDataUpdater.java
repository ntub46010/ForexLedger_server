package com.vincent.forexledger.util;

import com.vincent.forexledger.exception.InsufficientBalanceException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;

public class SingleBookMetaDataUpdater {
    private Book book;

    public SingleBookMetaDataUpdater(Book book) {
        this.book = book;
    }

    public void update(Entry entry) {
        var balance = calcBalance(entry);
        book.setBalance(balance);

        var transactionType = entry.getTransactionType();
        if (transactionType != TransactionType.TRANSFER_IN_FROM_INTEREST) {
            var remainingTwdFund = calcRemainingTwdFund(entry);
            book.setRemainingTwdFund(remainingTwdFund);
        }

        // TODO: unit test
        if (book.getBalance() == 0) {
            book.setBreakEvenPoint(null);
        } else {
            var breakEvenPoint = CalcUtil.divideToDouble(book.getRemainingTwdFund(), book.getBalance(), 4);
            book.setBreakEvenPoint(breakEvenPoint);
        }

        // TODO: unit test
        if (entry.getTransactionType().isTransferIn()
                && transactionType != TransactionType.TRANSFER_IN_FROM_INTEREST) {
            book.setLastForeignInvest(entry.getForeignAmount());
            book.setLastTwdInvest(entry.getTwdAmount());
        }
    }

    private double calcBalance(Entry entry) {
        var currentBalance = book.getBalance();
        if (entry.getTransactionType().isTransferIn()) {
            return CalcUtil.addToDouble(currentBalance, entry.getForeignAmount());
        }

        if (currentBalance < entry.getForeignAmount()) {
            throw new InsufficientBalanceException(currentBalance, entry.getForeignAmount());
        }

        return CalcUtil.subtractToDouble(currentBalance, entry.getForeignAmount());
    }

    private int calcRemainingTwdFund(Entry entry) {
        var result =  entry.getTransactionType().isTransferIn()
                ? book.getRemainingTwdFund() + entry.getTwdAmount()
                : book.getRemainingTwdFund() - entry.getTwdAmount();
        // TODO: unit test for down to 0
        return result > 0 ? result : 0;
    }
}
