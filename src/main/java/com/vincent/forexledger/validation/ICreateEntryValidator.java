package com.vincent.forexledger.validation;

import com.vincent.forexledger.model.entry.CreateEntryRequest;

public interface ICreateEntryValidator {
    boolean validate(CreateEntryRequest request);
}
