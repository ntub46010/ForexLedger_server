package com.vincent.forexledger.service;

import com.vincent.forexledger.exception.BadRequestException;
import com.vincent.forexledger.exception.InsufficientBalanceException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.util.converter.EntryConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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

    // TODO: unit test
    @Transactional
    public String createEntry2(CreateEntryRequest request) {
        validate(request);

        var involvedBookIds = Stream.of(request.getBookId(), request.getRelatedBookId())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        var isRelatedToAnotherBook = involvedBookIds.size() > 1;

        var entries = new ArrayList<Entry>(involvedBookIds.size());
        var books = bookService.loadBooksByIds(involvedBookIds);
        var bookMap = books.stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        var primaryEntry = EntryConverter.toEntry(request);
        primaryEntry.setCreator(userIdentity.getId());
        primaryEntry.setCreatedTime(new Date());
        entries.add(primaryEntry);

        if (isRelatedToAnotherBook) {
            var relatedBook = bookMap.get(request.getRelatedBookId());
            var relatedEntry = toRelatedBookEntry(relatedBook, primaryEntry);
            entries.add(relatedEntry);
            repository.insert(List.of(primaryEntry, relatedEntry));
        } else {
            repository.insert(primaryEntry);
        }

        var bookToEntryMap = entries.stream()
                .collect(Collectors.toMap(entry -> bookMap.get(entry.getBookId()), Function.identity()));
        bookService.updateMetaData2(bookToEntryMap);

        return primaryEntry.getId();
    }

    @Transactional
    public String createEntry(CreateEntryRequest request) {
        validate(request);

        var entry = EntryConverter.toEntry(request);
        entry.setCreator(userIdentity.getId());
        entry.setCreatedTime(new Date());
        repository.insert(entry);

        bookService.updateMetaData(entry);

        return entry.getId();
    }

    private Entry toRelatedBookEntry(Book relatedBook, Entry primaryBookEntry) {
        if (primaryBookEntry.getTransactionType().isTransferIn()
                && relatedBook.getBalance() < primaryBookEntry.getRelatedBookForeignAmount()) {
            throw new InsufficientBalanceException(relatedBook.getBalance(), primaryBookEntry.getRelatedBookForeignAmount());
        }

        return EntryConverter
                .toRelatedBookEntry(relatedBook, primaryBookEntry);
    }

    // TODO: enhance
    private void validate(CreateEntryRequest request) {
        if (StringUtils.isEmpty(request.getRelatedBookId()) &&
                (request.getTransactionType() == TransactionType.TRANSFER_IN_FROM_FOREIGN ||
                request.getTransactionType() == TransactionType.TRANSFER_OUT_TO_FOREIGN)) {
                throw new BadRequestException("Id of related book is required.");
        }
    }
}
