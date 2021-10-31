package com.vincent.forexledger.model.entry;

public enum TransactionType {
    TRANSFER_IN_FROM_TWD(true),
    TRANSFER_IN_FROM_FOREIGN(true),
    TRANSFER_IN_FROM_INTEREST(true),
    TRANSFER_IN_FROM_OTHER(true),
    TRANSFER_OUT_TO_TWD(false),
    TRANSFER_OUT_TO_FOREIGN(false),
    TRANSFER_OUT_TO_OTHER(false);

    private boolean isTransferIn;

    TransactionType(boolean isTransferIn) {
        this.isTransferIn = isTransferIn;
    }

    public boolean isTransferIn() {
        return isTransferIn;
    }
}
