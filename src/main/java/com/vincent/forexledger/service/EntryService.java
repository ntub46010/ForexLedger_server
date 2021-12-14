package com.vincent.forexledger.service;

import com.vincent.forexledger.exception.BadRequestException;
import com.vincent.forexledger.exception.InsufficientBalanceException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.EntryListResponse;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.util.converter.EntryConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.vincent.forexledger.validation.EntryValidatorFactory;
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
        bookService.updateMetaData(bookToEntryMap);

        return primaryEntry.getId();
    }

    // TODO: unit test
    public List<EntryListResponse> loadBookEntries(String bookId) {
        var entries = repository.findByBookIdOrderByTransactionDateDesc(bookId);
        if (CollectionUtils.isEmpty(entries)) {
            return Collections.emptyList();
        }

        var involvedBookIds = entries.stream()
                .map(Entry::getRelatedBookId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        involvedBookIds.add(bookId);
        var involvedBooks = bookService.loadBooksByIds(involvedBookIds);
        var bookToCurrencyTypeMap = involvedBooks.stream()
                .collect(Collectors.toMap(Book::getId, Book::getCurrencyType));

        return entries.stream()
                .map(entry -> {
                    var response = EntryConverter.toEntryListResponse(entry);
                    response.setPrimaryCurrencyType(bookToCurrencyTypeMap.get(entry.getBookId()));
                    if (StringUtils.isNotBlank(entry.getRelatedBookId())) {
                        response.setRelatedCurrencyType(bookToCurrencyTypeMap.get(entry.getRelatedBookId()));
                    }
                    return response;
                })
                .collect(Collectors.toList());
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
