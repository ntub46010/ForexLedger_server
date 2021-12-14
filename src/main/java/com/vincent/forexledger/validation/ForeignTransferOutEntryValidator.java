package com.vincent.forexledger.validation;

import com.vincent.forexledger.model.entry.CreateEntryRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class ForeignTransferOutEntryValidator implements ICreateEntryValidator {

    @Override
    public boolean validate(CreateEntryRequest request) {
        if (request.getTwdAmount() != null) {
            return false;
        }

        var hasRelatedBookId = StringUtils.isNotBlank(request.getRelatedBookId());
        var hasRelatedForeignAmount = request.getRelatedBookForeignAmount() != null;
        if (hasRelatedBookId ^ hasRelatedForeignAmount) {
            return false;
        }

        var relatedForeignAmount = Optional.ofNullable(request.getRelatedBookForeignAmount())
                .orElse(0.0);

        return !hasRelatedBookId || relatedForeignAmount > 0;
    }
}
