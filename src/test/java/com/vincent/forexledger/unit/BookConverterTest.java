package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.util.converter.BookConverter;
import org.junit.Assert;
import org.junit.Test;

public class BookConverterTest {

    @Test
    public void testConvertCreatingBook() {
        var request = new CreateBookRequest();
        request.setName("Book Name");
        request.setBank(BankType.FUBON);
        request.setCurrencyType(CurrencyType.USD);

        var book = BookConverter.toBook(request);

        Assert.assertEquals(request.getName(), book.getName());
        Assert.assertEquals(request.getBank(), book.getBank());
        Assert.assertEquals(request.getCurrencyType(), book.getCurrencyType());
    }
}
