package com.vincent.forexledger.service;

import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.repository.BookRepository;

public class BookService {
    private BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public String createBook(CreateBookRequest request) {
        return "123";
    }
}
