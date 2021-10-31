package com.vincent.forexledger.service;

import com.vincent.forexledger.exception.BadRequestException;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.TransactionType;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.util.converter.EntryConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class EntryService {
    private UserIdentity userIdentity;
    private EntryRepository repository;
    private BookService bookService;

    public EntryService(UserIdentity userIdentity, EntryRepository repository, BookService bookService) {
        this.userIdentity = userIdentity;
        this.repository = repository;
        this.bookService = bookService;
    }

    public String createEntry(CreateEntryRequest request) {
        validate(request);

        var entry = EntryConverter.toEntry(request);
        entry.setCreator(userIdentity.getId());
        entry.setCreatedTime(new Date());
        repository.insert(entry);

        // TODO: write data in book

        return entry.getId();
    }

    private void validate(CreateEntryRequest request) {
        if (StringUtils.isEmpty(request.getAnotherBookId()) &&
                (request.getTransactionType() == TransactionType.TRANSFER_IN_FROM_FOREIGN ||
                request.getTransactionType() == TransactionType.TRANSFER_OUT_TO_FOREIGN)) {
                throw new BadRequestException("Id of another book is required.");
        }
    }
}
