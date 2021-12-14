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
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;

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
        Assert.assertEquals(0, book.getRemainingTwdFund());
        Assert.assertNull(book.getBreakEvenPoint());
        Assert.assertNull(book.getLastForeignInvest());
        Assert.assertNull(book.getLastTwdInvest());
    }

    @Test
    public void testConvertBookListResponse() {
        BiConsumer<Book, BookListResponse> assertFunc = (expected, actual) -> {
            Assert.assertEquals(expected.getId(), actual.getId());
            Assert.assertEquals(expected.getName(), actual.getName());
            Assert.assertEquals(expected.getCurrencyType(), actual.getCurrencyType());
            Assert.assertEquals(expected.getBalance(), actual.getBalance(), 0);
            Assert.assertEquals(-347, actual.getTwdProfit(), 1);
            Assert.assertEquals(-0.0145, actual.getProfitRate(), 0);
        };

        var book = new Book();
        book.setId(ObjectId.get().toString());
        book.setName("Book Name");
        book.setBank(BankType.FUBON);
        book.setCurrencyType(CurrencyType.GBP);
        book.setBalance(621.77);
        book.setRemainingTwdFund(23877);

        var response = BookConverter.toBookListResponse(book, 37.8428);
        assertFunc.accept(book, response);

        var responses = BookConverter.toBookListResponses(
                List.of(book),
                Map.of(BankType.FUBON, Map.of(CurrencyType.GBP, 37.8428)));
        Assert.assertEquals(1, responses.size());
        assertFunc.accept(book, responses.get(0));
    }

    @Test
    public void testConvertEmptyBookDetailResponse() {
        var book = new Book();
        book.setId(ObjectId.get().toString());
        book.setName("Book Name");
        book.setBank(BankType.FUBON);
        book.setCurrencyType(CurrencyType.ZAR);
        book.setBalance(0);
        book.setRemainingTwdFund(0);
        book.setBreakEvenPoint(null);
        book.setLastForeignInvest(null);
        book.setLastTwdInvest(null);

        var bankBuyingRate = 1.8507;
        var detail = BookConverter.toBookDetail(book, bankBuyingRate);

        Assert.assertEquals(book.getId(), detail.getId());
        Assert.assertEquals(book.getCurrencyType(), detail.getCurrencyType());
        Assert.assertEquals(bankBuyingRate, detail.getBankBuyingRate(), 0);
        Assert.assertEquals(book.getBalance(), detail.getBalance(), 0);
        Assert.assertNull(detail.getTwdProfit());
        Assert.assertNull(detail.getTwdProfitRate());
        Assert.assertNull(detail.getBreakEvenPoint());
        Assert.assertNull(detail.getLastForeignInvest());
        Assert.assertNull(detail.getLastTwdInvest());
        Assert.assertEquals(0, detail.getTwdCurrentValue());
        Assert.assertNull(detail.getLastSellingRate());
    }

    @Test
    public void testConvertNotEmptyBookDetailResponse() {
        var book = new Book();
        book.setId(ObjectId.get().toString());
        book.setName("Book Name");
        book.setBank(BankType.FUBON);
        book.setCurrencyType(CurrencyType.ZAR);
        book.setBalance(51475.54);
        book.setRemainingTwdFund(97148);
        book.setBreakEvenPoint(1.8873);
        book.setLastForeignInvest(33489.9);
        book.setLastTwdInvest(63533);

        var bankBuyingRate = 1.8507;
        var detail = BookConverter.toBookDetail(book, bankBuyingRate);

        Assert.assertEquals(book.getId(), detail.getId());
        Assert.assertEquals(book.getCurrencyType(), detail.getCurrencyType());
        Assert.assertEquals(bankBuyingRate, detail.getBankBuyingRate(), 0);
        Assert.assertEquals(book.getBalance(), detail.getBalance(), 0);
        Assert.assertEquals(-1882, detail.getTwdProfit(), 1);
        Assert.assertEquals(-0.0194, detail.getTwdProfitRate(), 1);
        Assert.assertEquals(book.getBreakEvenPoint(), detail.getBreakEvenPoint());
        Assert.assertEquals(book.getLastForeignInvest(), detail.getLastForeignInvest());
        Assert.assertEquals(book.getLastTwdInvest(), detail.getLastTwdInvest());
        Assert.assertEquals(95266, detail.getTwdCurrentValue(), 1);
        Assert.assertEquals(1.8971, detail.getLastSellingRate(), 0.0001);
    }

    @Test
    public void testCalcRepresentingTwdFund () {
        var book = new Book();
        book.setBalance(3000);
        book.setRemainingTwdFund(92531);

        assertEquals(61687, BookConverter.calcRepresentingTwdFund(book, 2000));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalcRepresentingTwdFundButForeignAmountExceeds() {
        var book = new Book();
        book.setBalance(3000);
        book.setRemainingTwdFund(92531);

        BookConverter.calcRepresentingTwdFund(book, 6000);
    }

}