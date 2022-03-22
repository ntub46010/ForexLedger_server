package com.vincent.forexledger.util;

import com.vincent.forexledger.exception.InsufficientBalanceException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;

@Deprecated
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

        var primaryBookBalance = calcPrimaryBookBalance(entry);
        var relatedBookBalance = calcRelatedBookBalance(entry);
        var primaryBookRemainingTwdFund = calcPrimaryBookRemainingTwdFund(entry);
        var relatedBookRemainingTwdFund = calcRelatedBookRemainingTwdFund(entry);

        if (entry.getTransactionType().isTransferIn()) {
            primaryBook.setLastForeignInvest(entry.getForeignAmount());
            primaryBook.setLastTwdInvest(primaryBookRemainingTwdFund - primaryBook.getRemainingTwdFund());
        } else {
            relatedBook.setLastForeignInvest(entry.getRelatedBookForeignAmount());
            relatedBook.setLastTwdInvest(relatedBookRemainingTwdFund - relatedBook.getRemainingTwdFund());
        }

        primaryBook.setRemainingTwdFund(primaryBookRemainingTwdFund);
        relatedBook.setRemainingTwdFund(relatedBookRemainingTwdFund);
        primaryBook.setBalance(primaryBookBalance);
        relatedBook.setBalance(relatedBookBalance);

        if (entry.getTransactionType().isTransferIn()) {
            var primaryBreakEvenPoint =
                    CalcUtil.divideToDouble(primaryBook.getRemainingTwdFund(), primaryBook.getBalance(), 4);
            primaryBook.setBreakEvenPoint(primaryBreakEvenPoint);
        } else {
            var relatedBreakEvenPoint =
                    CalcUtil.divideToDouble(relatedBook.getRemainingTwdFund(), relatedBook.getBalance(), 4);
            relatedBook.setBreakEvenPoint(relatedBreakEvenPoint);
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
            return CalcUtil.addToDouble(relatedBook.getBalance(), entry.getRelatedBookForeignAmount());
        }

        if (relatedBook.getBalance() < entry.getRelatedBookForeignAmount()) {
            throw new InsufficientBalanceException(relatedBook.getBalance(), entry.getRelatedBookForeignAmount());
        }
        return CalcUtil.subtractToDouble(relatedBook.getBalance(), entry.getRelatedBookForeignAmount());
    }

    private int calcPrimaryBookRemainingTwdFund(Entry entry) {
        if (entry.getTransactionType().isTransferIn()) {
            var deltaTwdFund = CalcUtil.divideToInt(
                    CalcUtil.multiplyToDecimal(relatedBook.getRemainingTwdFund(), entry.getRelatedBookForeignAmount()),
                    relatedBook.getBalance()
            );

            return primaryBook.getRemainingTwdFund() + deltaTwdFund;
        }

        if (primaryBook.getBalance() < entry.getForeignAmount()) {
            throw new InsufficientBalanceException(primaryBook.getBalance(), entry.getForeignAmount());
        }

        var deltaTwdFund = CalcUtil.divideToInt(
                CalcUtil.multiplyToDecimal(primaryBook.getRemainingTwdFund(), entry.getForeignAmount()),
                primaryBook.getBalance()
        );

        return primaryBook.getRemainingTwdFund() - deltaTwdFund;
    }

    private int calcRelatedBookRemainingTwdFund(Entry entry) {
        if (!entry.getTransactionType().isTransferIn()) {
            var deltaTwdFund = CalcUtil.divideToInt(
                    CalcUtil.multiplyToDecimal(primaryBook.getRemainingTwdFund(), entry.getForeignAmount()),
                    primaryBook.getBalance()
            );

            return relatedBook.getRemainingTwdFund() + deltaTwdFund;
        }

        if (relatedBook.getBalance() < entry.getRelatedBookForeignAmount()) {
            throw new InsufficientBalanceException(relatedBook.getBalance(), entry.getRelatedBookForeignAmount());
        }

        var deltaTwdFund = CalcUtil.divideToInt(
                CalcUtil.multiplyToDecimal(relatedBook.getRemainingTwdFund(), entry.getRelatedBookForeignAmount()),
                relatedBook.getBalance()
        );

        return relatedBook.getRemainingTwdFund() - deltaTwdFund;
    }

}
