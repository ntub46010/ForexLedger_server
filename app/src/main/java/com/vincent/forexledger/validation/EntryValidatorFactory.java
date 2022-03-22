package com.vincent.forexledger.validation;

import com.vincent.forexledger.model.entry.TransactionType;

import java.util.EnumMap;
import java.util.Map;

public class EntryValidatorFactory {
    private static final Map<TransactionType, ICreateEntryValidator> validatorMap;

    static {
        validatorMap = new EnumMap<>(TransactionType.class);
        var twdEntryValidator = new TwdEntryValidator();

        validatorMap.put(TransactionType.TRANSFER_IN_FROM_TWD, twdEntryValidator);
        validatorMap.put(TransactionType.TRANSFER_OUT_TO_TWD, twdEntryValidator);
        validatorMap.put(TransactionType.TRANSFER_IN_FROM_FOREIGN, new ForeignTransferInEntryValidator());
        validatorMap.put(TransactionType.TRANSFER_OUT_TO_FOREIGN, new ForeignTransferOutEntryValidator());
        validatorMap.put(TransactionType.TRANSFER_IN_FROM_INTEREST, new InterestEntryValidator());
        validatorMap.put(TransactionType.TRANSFER_IN_FROM_OTHER, new OtherTransferInEntryValidator());
        validatorMap.put(TransactionType.TRANSFER_OUT_TO_OTHER, new OtherTransferOutEntryValidator());
    }

    private EntryValidatorFactory() {}

    public static ICreateEntryValidator getCreateEntryValidator(TransactionType type) {
        return validatorMap.get(type);
    }
}
