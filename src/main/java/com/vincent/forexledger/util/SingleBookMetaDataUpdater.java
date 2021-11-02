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
        var balance = calcBalance(entry);
        book.setBalance(balance);

        var remainingTwdFund = calcRemainingTwdFund(entry);
        book.setRemainingTwdFund(remainingTwdFund);

        var breakEvenPoint = CalcUtil.divideToDouble(book.getRemainingTwdFund(), book.getBalance(), 4);
        book.setBreakEvenPoint(breakEvenPoint);

        if (entry.getTransactionType().isTransferIn()) {
            book.setLastForeignInvest(entry.getForeignAmount());
            book.setLastTwdInvest(entry.getTwdAmount());
        }
    }

    private double calcBalance(Entry entry) {
        var currentBalance = book.getBalance();
        if (entry.getTransactionType().isTransferIn()) {
            return CalcUtil.addToDouble(currentBalance, entry.getForeignAmount());
        } else {
            if (currentBalance < entry.getForeignAmount()) {
                throw new InsufficientBalanceException(currentBalance, entry.getForeignAmount());
            }

            return CalcUtil.subtractToDouble(currentBalance, entry.getForeignAmount());
        }
    }

    private int calcRemainingTwdFund(Entry entry) {
        return entry.getTransactionType().isTransferIn()
                ? book.getRemainingTwdFund() + entry.getTwdAmount()
                : book.getRemainingTwdFund() - entry.getTwdAmount();
    }
}
