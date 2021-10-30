package com.vincent.forexledger.repository;

import com.vincent.forexledger.model.entry.Entry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends MongoRepository<Entry, String> {
}
