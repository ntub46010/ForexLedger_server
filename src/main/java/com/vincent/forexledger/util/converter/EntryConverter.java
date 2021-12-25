package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.backup.BookAndEntryBackup;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.Entry;
import com.vincent.forexledger.model.entry.EntryListResponse;
import com.vincent.forexledger.model.entry.TransactionType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class EntryConverter {
    private EntryConverter() {
    }

    public static Entry toEntry(CreateEntryRequest request) {
        var entry = new Entry();
        entry.setBookId(request.getBookId());
        entry.setTransactionType(request.getTransactionType());
        entry.setTransactionDate(request.getTransactionDate());
        entry.setDescription(request.getDescription());
        entry.setForeignAmount(request.getForeignAmount());
        entry.setTwdAmount(request.getTwdAmount());
        entry.setRelatedBookId(request.getRelatedBookId());
        entry.setRelatedBookForeignAmount(request.getRelatedBookForeignAmount());

        return entry;
    }

    // TODO: unit test
    public static Entry toEntry(BookAndEntryBackup.EntryBackup backup) {
        var entry = new Entry();
        entry.setId(backup.getId());
        entry.setTransactionType(backup.getTransactionType());
        entry.setTransactionDate(backup.getTransactionDate());
        entry.setForeignAmount(backup.getForeignAmount());
        entry.setTwdAmount(backup.getTwdAmount());
        entry.setDescription(backup.getDescription());
        entry.setRelatedBookId(backup.getRelatedBookId());
        entry.setRelatedBookForeignAmount(backup.getRelatedBookForeignAmount());
        entry.setCreatedTime(backup.getCreatedTime());

        return entry;
    }

    public static Entry toRelatedEntry(Entry primaryEntry) {
        var entry = new Entry();
        if (primaryEntry.getTransactionType() == TransactionType.TRANSFER_IN_FROM_FOREIGN) {
            entry.setTransactionType(TransactionType.TRANSFER_OUT_TO_FOREIGN);
        } else if (primaryEntry.getTransactionType() == TransactionType.TRANSFER_OUT_TO_FOREIGN) {
            entry.setTransactionType(TransactionType.TRANSFER_IN_FROM_FOREIGN);
        } else {
            throw new IllegalArgumentException("Transaction type of primary entry is unexpected.");
        }

        entry.setBookId(primaryEntry.getRelatedBookId());
        entry.setTransactionDate(primaryEntry.getTransactionDate());
        entry.setForeignAmount(primaryEntry.getRelatedBookForeignAmount());
        entry.setRelatedBookId(primaryEntry.getBookId());
        entry.setRelatedBookForeignAmount(primaryEntry.getForeignAmount());
        entry.setCreator(primaryEntry.getCreator());
        entry.setCreatedTime(primaryEntry.getCreatedTime());

        return entry;
    }

    // TODO: unit test
    public static List<EntryListResponse> toEntryListResponses(List<Entry> entries, Map<String, CurrencyType> bookToCurrencyTypeMap) {
        var responses = new ArrayList<EntryListResponse>(entries.size());

        for (var entry : entries) {
            var response = new EntryListResponse();
            response.setId(entry.getId());
            response.setTransactionDate(entry.getTransactionDate());
            response.setTransactionType(entry.getTransactionType());
            response.setPrimaryAmount(entry.getForeignAmount());
            response.setDescription(entry.getDescription());
            response.setPrimaryCurrencyType(bookToCurrencyTypeMap.get(entry.getBookId()));

            if (StringUtils.isNotBlank(entry.getRelatedBookId())) {
                response.setRelatedCurrencyType(bookToCurrencyTypeMap.get(entry.getRelatedBookId()));
                response.setRelatedAmount(entry.getRelatedBookForeignAmount());
            } else {
                Optional.ofNullable(entry.getTwdAmount())
                        .ifPresent(x -> response.setRelatedAmount(x.doubleValue()));
            }

            responses.add(response);
        }

        return responses;
    }

    // TODO: unit test
    public static List<BookAndEntryBackup.EntryBackup> toEntryBackup(List<Entry> entries) {
        return entries.stream()
                .sorted(Comparator.comparing(Entry::getTransactionDate)
                        .thenComparing(Entry::getCreatedTime)
                        .thenComparing(e -> !e.getTransactionType().isTransferIn())
                )
                .map(EntryConverter::toEntryBackup)
                .collect(Collectors.toList());
    }

    private static BookAndEntryBackup.EntryBackup toEntryBackup(Entry entry) {
        var backup = new BookAndEntryBackup.EntryBackup();
        backup.setId(entry.getId());
        backup.setTransactionType(entry.getTransactionType());
        backup.setTransactionDate(entry.getTransactionDate());
        backup.setForeignAmount(entry.getForeignAmount());
        backup.setTwdAmount(entry.getTwdAmount());
        backup.setDescription(entry.getDescription());
        backup.setRelatedBookId(entry.getRelatedBookId());
        backup.setRelatedBookForeignAmount(entry.getRelatedBookForeignAmount());
        backup.setCreatedTime(entry.getCreatedTime());

        return backup;
    }
}
