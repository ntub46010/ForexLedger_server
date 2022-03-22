package com.vincent.forexledger.model.entry;

public enum TransactionType {
    TRANSFER_IN_FROM_TWD(true, false, true),
    TRANSFER_OUT_TO_TWD(false, false, true),
    TRANSFER_IN_FROM_FOREIGN(true, true, false),
    TRANSFER_OUT_TO_FOREIGN(false, true, false),
    TRANSFER_IN_FROM_INTEREST(true, false, false),
    TRANSFER_IN_FROM_OTHER(true, false, false),
    TRANSFER_OUT_TO_OTHER(false, false, false);

    private boolean isTransferIn;
    private boolean canRelateBook;
    private boolean isRelatedToTwd;

    TransactionType(boolean isTransferIn, boolean canRelateBook, boolean isRelatedToTwd) {
        this.isTransferIn = isTransferIn;
        this.canRelateBook = canRelateBook;
        this.isRelatedToTwd = isRelatedToTwd;
    }

    public boolean isTransferIn() {
        return isTransferIn;
    }

    public boolean canRelateBook() {
        return canRelateBook;
    }

    public boolean isRelatedToTwd() {
        return isRelatedToTwd;
    }
}
