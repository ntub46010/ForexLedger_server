package com.vincent.forexledger.service;

import com.vincent.forexledger.exception.BadRequestException;
import com.vincent.forexledger.exception.InsufficientBalanceException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.util.converter.BookConverter;
import com.vincent.forexledger.util.converter.EntryConverter;
import com.vincent.forexledger.validation.EntryValidatorFactory;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntryService {
    private UserIdentity userIdentity;
    private EntryRepository repository;
    private BookService bookService;

    public EntryService(UserIdentity userIdentity, EntryRepository repository, BookService bookService) {
        this.userIdentity = userIdentity;
        this.repository = repository;
        this.bookService = bookService;
    }

    @Transactional
    public String createEntry(CreateEntryRequest request) {
        validate(request);

        var involvedBookIds = Stream.of(request.getBookId(), request.getRelatedBookId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        var bookMap = bookService.loadBooksByIds(involvedBookIds)
                .stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        var bookToEntryMap = new HashMap<Book, Entry>();

        var primaryEntry = EntryConverter.toEntry(request);
        primaryEntry.setCreator(userIdentity.getId());
        primaryEntry.setCreatedTime(new Date());
        bookToEntryMap.put(bookMap.get(request.getBookId()), primaryEntry);

        if (request.getRelatedBookId() != null) {
            var relatedBook = bookMap.get(request.getRelatedBookId());
            var relatedEntry = EntryConverter.toRelatedEntry(primaryEntry);
            bookToEntryMap.put(relatedBook, relatedEntry);
        }

        validateBalanceIsSufficient(bookToEntryMap);
        repository.insert(bookToEntryMap.values());

        assignRepresentingTwdFundIfAbsent(bookToEntryMap);
        bookService.updateMetaData(bookToEntryMap);

        return primaryEntry.getId();
    }

    private void assignRepresentingTwdFundIfAbsent(Map<Book, Entry> bookToEntryMap) {
        Pair<Book, Entry> transferOutInfo = null;
        for (var pair : bookToEntryMap.entrySet()) {
            var entry = pair.getValue();
            if (!entry.getTransactionType().isTransferIn() && entry.getTwdAmount() == null) {
                transferOutInfo = Pair.of(pair.getKey(), entry);
            }
        }

        if (transferOutInfo == null) {
            return;
        }

        var twdFund = BookConverter.calcRepresentingTwdFund(
                transferOutInfo.getFirst(),
                transferOutInfo.getSecond().getForeignAmount());

        bookToEntryMap.values().stream()
                .filter(entry -> entry.getTwdAmount() == null)
                .forEach(entry -> entry.setTwdAmount(twdFund));
    }

    private void validate(CreateEntryRequest request) {
        var validator = EntryValidatorFactory
                .getCreateEntryValidator(request.getTransactionType());
        var isNotValid = !validator.validate(request);

        if (isNotValid) {
            var msg = String.format("Incorrect data for entry of %s type.",
                    request.getTransactionType().name());
            throw new BadRequestException(msg);
        }
    }

    private void validateBalanceIsSufficient(Map<Book, Entry> bookToEntryMap) {
        for (Map.Entry<Book, Entry> pair : bookToEntryMap.entrySet()) {
            var entry = pair.getValue();
            if (!entry.getTransactionType().isTransferIn()) {
                var book = pair.getKey();
                if (entry.getForeignAmount() > book.getBalance()) {
                    throw new InsufficientBalanceException(book.getBalance(), entry.getForeignAmount());
                }
            }
        }
    }
}
