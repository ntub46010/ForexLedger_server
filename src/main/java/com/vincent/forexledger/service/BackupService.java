package com.vincent.forexledger.service;

import com.vincent.forexledger.model.backup.BookAndEntryBackup;
import com.vincent.forexledger.repository.EntryRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.util.converter.BookConverter;
import com.vincent.forexledger.util.converter.EntryConverter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.stream.Collectors;

public class BackupService {
    private UserIdentity userIdentity;
    private BookService bookService;
    private EntryRepository entryRepository;

    public BackupService(UserIdentity userIdentity, BookService bookService,
                         EntryRepository entryRepository) {
        this.userIdentity = userIdentity;
        this.bookService = bookService;
        this.entryRepository = entryRepository;
    }

    public BookAndEntryBackup backupBookAndEntry(String bookId) {
        var book = bookService.loadBookById(bookId);
        var bookBackup = BookConverter.toBookBackup(book);

        var entries = entryRepository.findByBookId(bookId);
        var entriesBackup = EntryConverter.toEntryBackup(entries);

        return new BookAndEntryBackup(bookBackup, entriesBackup);
    }

    public void restoreBookAndEntry(BookAndEntryBackup backup) {
        var book = BookConverter.toBook(backup.getBook());
        book.setCreator(userIdentity.getId());
        bookService.saveBook(book);

        if (CollectionUtils.isEmpty(backup.getEntries())) {
            return;
        }

        var entries = backup.getEntries().stream()
                .map(EntryConverter::toEntry)
                .peek(entry -> {
                    entry.setBookId(book.getId());
                    entry.setCreator(userIdentity.getId());
                })
                .collect(Collectors.toList());
        
        entryRepository.saveAll(entries);
    }
}
