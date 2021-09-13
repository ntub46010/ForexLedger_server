package com.vincent.forexledger.repository;

import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface ExchangeRateRepository extends MongoRepository<ExchangeRate, String> {
    void deleteByBankTypeIn(Collection<BankType> banks);
}
