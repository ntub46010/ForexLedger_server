package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.util.converter.BookConverter;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;

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
        Assert.assertEquals(0, book.getBalance(), 0);
        Assert.assertNull(book.getTwdProfit());
        Assert.assertNull(book.getProfitRate());
    }

    @Test
    public void testConvertBookListResponse() {
        BiConsumer<Book, BookListResponse> assertFunc = (expected, actual) -> {
            Assert.assertEquals(expected.getId(), actual.getId());
            Assert.assertEquals(expected.getName(), actual.getName());
            Assert.assertEquals(expected.getCurrencyType(), actual.getCurrencyType());
            Assert.assertEquals(expected.getBalance(), actual.getBalance(), 0);
            Assert.assertEquals(expected.getTwdProfit(), actual.getTwdProfit());
            Assert.assertEquals(expected.getProfitRate(), actual.getProfitRate());
        };

        var book = new Book();
        book.setId(ObjectId.get().toString());
        book.setName("Book Name");
        book.setCurrencyType(CurrencyType.USD);
        book.setBalance(1947.33);
        book.setTwdProfit(-3671);
        book.setProfitRate(-0.0189);

        var response = BookConverter.toBookListResponse(book);
        assertFunc.accept(book, response);

        var responses = BookConverter.toBookListResponses(List.of(book));
        Assert.assertEquals(1, responses.size());
        assertFunc.accept(book, responses.get(0));
    }
}
