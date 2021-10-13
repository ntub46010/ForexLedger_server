package com.vincent.forexledger.service;

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

    public BookService(UserIdentity userIdentity, BookRepository repository) {
        this.userIdentity = userIdentity;
        this.repository = repository;
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
}
