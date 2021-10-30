package com.vincent.forexledger.service;

import com.vincent.forexledger.repository.EntryRepository;

public class EntryService {
    private EntryRepository repository;
    private BookService bookService;

    public EntryService(EntryRepository repository, BookService bookService) {
        this.repository = repository;
        this.bookService = bookService;
    }
}
