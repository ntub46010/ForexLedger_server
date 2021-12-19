package com.vincent.forexledger.service;

import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.util.CalcUtil;
import com.vincent.forexledger.util.converter.BookConverter;
import org.springframework.data.util.Pair;

import java.util.Comparator;
import java.util.HashMap;
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
                .sorted(Comparator.comparing(Entry::getTransactionDate))
                .collect(Collectors.toList());
        var bookIds = entries.stream()
                .map(Entry::getBookId)
                .collect(Collectors.toSet());

        var booksIterable = bookRepository.findAllById(bookIds);
        var bookMap = StreamSupport.stream(booksIterable.spliterator(), false)
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        var balanceMap = new HashMap<String, Double>();
        var twdFundMap = new HashMap<String, Integer>();
        var lastInvestMap = new HashMap<String, Pair<Double, Integer>>();
        entries.forEach(entry -> {
            // balance
            var balance = balanceMap.getOrDefault(entry.getBookId(), 0.0);
            if (entry.getTransactionType().isTransferIn()) {
                balance += entry.getForeignAmount();
            } else {
                balance -= entry.getForeignAmount();
            }
            balanceMap.put(entry.getBookId(), balance);

            // remaining TWD fund
            var twdFund = twdFundMap.get(entry.getBookId());
            int deltaTwdFund;
            if (entry.getTwdAmount() != null) {
                deltaTwdFund = entry.getTransactionType().isTransferIn()
                        ? entry.getTwdAmount()
                        : -entry.getTwdAmount();
            } else {
                var relatedBook = bookMap.get(entry.getRelatedBookId());
                deltaTwdFund = entry.getTransactionType().isTransferIn()
                        ? BookConverter.calcRepresentingTwdFund(relatedBook, entry.getRelatedBookForeignAmount())
                        : -BookConverter.calcRepresentingTwdFund(relatedBook, entry.getRelatedBookForeignAmount());
            }
            twdFund += deltaTwdFund;
            twdFundMap.put(entry.getBookId(), twdFund);

            // break even point
            var breakEvenPoint = CalcUtil.divideToDouble(twdFund, balance, 4);

            // last invest
            if (entry.getTransactionType().isTransferIn()) {
                lastInvestMap.put(entry.getBookId(), Pair.of(entry.getForeignAmount(), deltaTwdFund));
            }

            var book = bookMap.get(entry.getBookId());
            book.setBalance(balance);
            book.setRemainingTwdFund(twdFund);
            book.setBreakEvenPoint(breakEvenPoint);

            var lastInvestInfo = lastInvestMap.get(entry.getBookId());
            book.setLastForeignInvest(lastInvestInfo.getFirst());
            book.setLastTwdInvest(lastInvestInfo.getSecond());
        });

        bookRepository.saveAll(bookMap.values());
    }
}
