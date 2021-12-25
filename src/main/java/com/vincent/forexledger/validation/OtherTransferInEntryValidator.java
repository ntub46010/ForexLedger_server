package com.vincent.forexledger.validation;

import com.vincent.forexledger.model.entry.CreateEntryRequest;
import org.apache.commons.lang3.StringUtils;

public class OtherTransferInEntryValidator implements ICreateEntryValidator {
    
    @Override
    public boolean validate(CreateEntryRequest request) {
        if (request.getTwdAmount() == null || request.getTwdAmount() <= 0) {
            return false;
        }

        if (request.getRelatedBookForeignAmount() != null) {
            return false;
        }

        return StringUtils.isBlank(request.getRelatedBookId());
    }
}
