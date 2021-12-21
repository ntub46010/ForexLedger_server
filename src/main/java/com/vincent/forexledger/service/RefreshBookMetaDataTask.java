package com.vincent.forexledger.service;

import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.util.CalcUtil;
import com.vincent.forexledger.util.converter.BookConverter;
import org.springframework.data.util.Pair;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RefreshBookMetaDataTask {
    private BookRepository bookRepository;
    private EntryRepository entryRepository;

    public RefreshBookMetaDataTask(BookRepository bookRepository, EntryRepository entryRepository) {
        this.bookRepository = bookRepository;
        this.entryRepository = entryRepository;
    }

    public void process() {
        var entries = entryRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Entry::getTransactionDate)
                        .thenComparing(Entry::getCreatedTime))
                .collect(Collectors.toList());
        var bookIds = entries.stream()
                .map(Entry::getBookId)
                .collect(Collectors.toSet());

        var bookMap = loadBookMap(bookIds);
        var balanceMap = new HashMap<String, Double>();
        var twdFundMap = new HashMap<String, Integer>();
        var lastInvestMap = new HashMap<String, Pair<Double, Integer>>();

        entries.forEach(entry -> {
            // balance
            var balance = balanceMap.getOrDefault(entry.getBookId(), 0.0);
            var deltaBalance = calcDeltaBalance(entry);
            balance += deltaBalance;
            balanceMap.put(entry.getBookId(), balance);

            // remaining TWD fund
            var twdFund = twdFundMap.get(entry.getBookId());
            int deltaTwdFund = calcDeltaTwdFund(entry, bookMap);
            twdFund += deltaTwdFund;
            twdFundMap.put(entry.getBookId(), twdFund);

            // last invest
            if (entry.getTransactionType().isTransferIn()
                    || entry.getTransactionType() != TransactionType.TRANSFER_IN_FROM_INTEREST) {
                lastInvestMap.put(entry.getBookId(), Pair.of(deltaBalance, deltaTwdFund));
            }
        });

        bookMap.values().forEach(book -> {
            book.setBalance(balanceMap.get(book.getId()));
            book.setRemainingTwdFund(twdFundMap.get(book.getId()));
            book.setBreakEvenPoint(
                    CalcUtil.divideToDouble(book.getRemainingTwdFund(), book.getBalance(), 4));

            var lastInvestInfo = lastInvestMap.get(book.getId());
            book.setLastForeignInvest(lastInvestInfo.getFirst());
            book.setLastTwdInvest(lastInvestInfo.getSecond());
        });

        bookRepository.saveAll(bookMap.values());
    }

    private Map<String, Book> loadBookMap(Collection<String> bookIds) {
        var booksIterable = bookRepository.findAllById(bookIds);
        return StreamSupport.stream(booksIterable.spliterator(), false)
                .collect(Collectors.toMap(Book::getId, Function.identity()));
    }

    private double calcDeltaBalance(Entry entry) {
        return entry.getTransactionType().isTransferIn()
                ? entry.getForeignAmount()
                : -entry.getForeignAmount();
    }

    private int calcDeltaTwdFund(Entry entry, Map<String, Book> bookMap) {
        if (entry.getTwdAmount() != null) {
            return entry.getTransactionType().isTransferIn()
                    ? entry.getTwdAmount()
                    : -entry.getTwdAmount();
        } else {
            var relatedBook = bookMap.get(entry.getRelatedBookId());
            return entry.getTransactionType().isTransferIn()
                    ? BookConverter.calcRepresentingTwdFund(relatedBook, entry.getRelatedBookForeignAmount())
                    : -BookConverter.calcRepresentingTwdFund(relatedBook, entry.getRelatedBookForeignAmount());
        }
    }
}
