package com.vincent.forexledger.validation;

import com.vincent.forexledger.model.entry.CreateEntryRequest;
import org.apache.commons.lang3.StringUtils;

public class OtherTransferOutEntryValidator implements ICreateEntryValidator {

    @Override
    public boolean validate(CreateEntryRequest request) {
        var isValid = true;

        if (request.getTwdAmount() != null) {
            isValid = false;
        }

        if (StringUtils.isNotBlank(request.getRelatedBookId())) {
            isValid = false;
        }

        if (request.getRelatedBookForeignAmount() != null) {
            isValid = false;
        }

        return isValid;
    }
}
