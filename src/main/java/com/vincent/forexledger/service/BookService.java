package com.vincent.forexledger.service;

import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.util.converter.BookConverter;

import java.util.Date;

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
}
