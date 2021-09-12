package com.vincent.forexledger.repository;

import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExchangeRateRepository extends MongoRepository<ExchangeRate, String> {
}
