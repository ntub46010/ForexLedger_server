package com.vincent.forexledger.validation;

import com.vincent.forexledger.model.entry.CreateEntryRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class ForeignTransferInEntryValidator implements ICreateEntryValidator {

    @Override
    public boolean validate(CreateEntryRequest request) {
        var hasRelatedBookId = StringUtils.isNotBlank(request.getRelatedBookId());
        var hasRelatedForeignAmount = request.getRelatedBookForeignAmount() != null;
        if (hasRelatedBookId ^ hasRelatedForeignAmount) {
            return false;
        }

        if (hasRelatedBookId ^ request.getTwdAmount() == null) {
            return false;
        }

        var relatedForeignAmount = Optional.ofNullable(request.getRelatedBookForeignAmount())
                .orElse(0.0);
        if (hasRelatedBookId && relatedForeignAmount <= 0) {
            return false;
        }

        var twdAmount = Optional.ofNullable(request.getTwdAmount())
                .orElse(0);
        return hasRelatedBookId || twdAmount > 0;
    }
}
