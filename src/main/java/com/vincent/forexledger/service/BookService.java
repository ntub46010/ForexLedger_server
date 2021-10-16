package com.vincent.forexledger.service;

import com.vincent.forexledger.exception.NotFoundException;
import com.vincent.forexledger.model.book.BookDetailResponse;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.util.converter.BookConverter;

import java.util.Date;
import java.util.List;

public class BookService {
    private final UserIdentity userIdentity;
    private final BookRepository repository;
    private final ExchangeRateService exchangeRateService;

    public BookService(UserIdentity userIdentity, BookRepository repository,
                       ExchangeRateService exchangeRateService) {
        this.userIdentity = userIdentity;
        this.repository = repository;
        this.exchangeRateService = exchangeRateService;
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
        return BookConverter.toBookListResponses(books);
    }

    public BookDetailResponse loadBookDetail(String id) {
        var book = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Can't find book."));
        var exchangeRate = exchangeRateService.loadExchangeRate(book.getBank(), book.getCurrencyType());

        return BookConverter.toBookDetail(book, exchangeRate.getBuyingRate());
    }
}
