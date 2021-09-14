package com.vincent.forexledger.repository;

import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface ExchangeRateRepository extends MongoRepository<ExchangeRate, String> {
    List<ExchangeRate> findByBankTypeIn(Collection<BankType> banks);
}
