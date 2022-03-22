package com.vincent.forexledger.service;

import com.vincent.forexledger.exception.NotFoundException;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.book.BookDetailResponse;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.util.SingleBookMetaDataUpdater;
import com.vincent.forexledger.util.converter.BookConverter;
import org.springframework.data.util.Pair;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BookService {
    private final UserIdentity userIdentity;
    private final BookRepository repository;
    private final ExchangeRateTable exchangeRateTable;

    public BookService(UserIdentity userIdentity, BookRepository repository,
                       ExchangeRateTable exchangeRateTable) {
        this.userIdentity = userIdentity;
        this.repository = repository;
        this.exchangeRateTable = exchangeRateTable;
    }

    public String createBook(CreateBookRequest request) {
        var book = BookConverter.toBook(request);
        book.setCreator(userIdentity.getId());
        book.setCreatedTime(new Date());

        repository.insert(book);

        return book.getId();
    }

    public List<BookListResponse> loadMyBooks() {
        var books = repository.findByCreator(userIdentity.getId());
        var bankCurrencyTypePairs = books.stream()
                .map(book -> Pair.of(book.getBank(), book.getCurrencyType()))
                .collect(Collectors.toSet());

        return BookConverter.toBookListResponses(books, exchangeRateTable.getBuyingRates(bankCurrencyTypePairs));
    }

    // TODO: unit test
    public BookDetailResponse loadBookDetail(String id) {
        var book = loadBookById(id);
        var exchangeRate = exchangeRateTable.get(book.getBank(), book.getCurrencyType());

        return BookConverter.toBookDetail(book, exchangeRate.getBuyingRate());
    }

    public void updateMetaData(Map<Book, Entry> bookToEntryMap) {
        assignRepresentingTwdFundIfAbsent(bookToEntryMap);

        bookToEntryMap.forEach((book, entry) ->
                new SingleBookMetaDataUpdater(book).update(entry));
        repository.saveAll(bookToEntryMap.keySet());
    }

    public Book loadBookById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Can't find book."));
    }

    // TODO: unit test
    public List<Book> loadBooksByIds(Collection<String> bookIds) {
        return repository.findByIdIn(bookIds);
    }

    // TODO: unit test
    public void saveBook(Book book) {
        repository.save(book);
    }

    private void assignRepresentingTwdFundIfAbsent(Map<Book, Entry> bookToEntryMap) {
        Pair<Book, Entry> transferOutInfo = null;
        for (var pair : bookToEntryMap.entrySet()) {
            var entry = pair.getValue();
            if (!entry.getTransactionType().isTransferIn() && entry.getTwdAmount() == null) {
                transferOutInfo = Pair.of(pair.getKey(), entry);
                break;
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

}
