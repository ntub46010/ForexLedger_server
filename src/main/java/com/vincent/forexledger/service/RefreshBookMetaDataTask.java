package com.vincent.forexledger.service;

import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.util.CalcUtil;
import com.vincent.forexledger.util.converter.BookConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
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
                        .thenComparing(Entry::getCreatedTime)
                        .thenComparing(e -> !e.getTransactionType().isTransferIn())
                )
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
            balance = CalcUtil.addToDouble(balance, deltaBalance);

            // remaining TWD fund
            var twdFund = twdFundMap.getOrDefault(entry.getBookId(), 0);
            int deltaTwdFund = calcDeltaTwdFund(entry, balanceMap, twdFundMap);
            twdFund += deltaTwdFund;

            balanceMap.put(entry.getBookId(), balance);
            twdFundMap.put(entry.getBookId(), twdFund);

            // last invest
            if (entry.getTransactionType().isTransferIn()
                    && entry.getTransactionType() != TransactionType.TRANSFER_IN_FROM_INTEREST) {
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

    private int calcDeltaTwdFund(Entry entry, Map<String, Double> balanceMap, Map<String, Integer> twdFundMap) {
        if (entry.getTransactionType() == TransactionType.TRANSFER_IN_FROM_INTEREST) {
            return 0;
        }

        if (StringUtils.isBlank(entry.getRelatedBookId())) {
            return entry.getTransactionType().isTransferIn()
                    ? entry.getTwdAmount()
                    : -entry.getTwdAmount();
        } else {
            if (entry.getTransactionType().isTransferIn()) {
                var relatedBookBalance = balanceMap.get(entry.getRelatedBookId());
                var relatedBookTwdFund = twdFundMap.get(entry.getRelatedBookId());
                return BookConverter.calcRepresentingTwdFund(relatedBookTwdFund, relatedBookBalance, entry.getRelatedBookForeignAmount());
            } else {
                var primaryBookBalance = balanceMap.get(entry.getBookId());
                var primaryBookTwdFund = twdFundMap.get(entry.getBookId());
                return -BookConverter.calcRepresentingTwdFund(primaryBookTwdFund, primaryBookBalance, entry.getForeignAmount());
            }
        }
    }
}
