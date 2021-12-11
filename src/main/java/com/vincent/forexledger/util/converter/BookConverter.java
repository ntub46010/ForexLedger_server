package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.book.BookDetailResponse;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.util.CalcUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BookConverter {
    private BookConverter() {
    }

    public static Book toBook(CreateBookRequest request) {
        var book = new Book();
        book.setName(request.getName());
        book.setBank(request.getBank());
        book.setCurrencyType(request.getCurrencyType());
        book.setBalance(0);
        book.setRemainingTwdFund(0);

        return book;
    }

    public static List<BookListResponse> toBookListResponses(
            List<Book> books, Map<BankType, Map<CurrencyType, Double>> bankBuyingRateMap) {
        return books.stream()
                .map(book -> {
                    var buyingRate = bankBuyingRateMap.get(book.getBank()).get(book.getCurrencyType());
                    return toBookListResponse(book, buyingRate);
                })
                .collect(Collectors.toList());
    }

    public static BookListResponse toBookListResponse(Book book, double bankBuyingRate) {
        var response = new BookListResponse();
        response.setId(book.getId());
        response.setName(book.getName());
        response.setCurrencyType(book.getCurrencyType());
        response.setBalance(book.getBalance());

        var currentValue = CalcUtil.multiplyToInt(book.getBalance(), bankBuyingRate);
        if (book.getBalance() > 0) {
            var profit = currentValue - book.getRemainingTwdFund();
            var profitRate = CalcUtil.divideToDouble(profit, book.getRemainingTwdFund(), 4);
            response.setTwdProfit(profit);
            response.setProfitRate(profitRate);
        }

        return response;
    }

    public static BookDetailResponse toBookDetail(Book book, double bankBuyingRate) {
        var detail = new BookDetailResponse();
        detail.setId(book.getId());
        detail.setCurrencyType(book.getCurrencyType());
        detail.setBankBuyingRate(bankBuyingRate);
        detail.setBalance(book.getBalance());
        detail.setBreakEvenPoint(book.getBreakEvenPoint());
        detail.setLastForeignInvest(book.getLastForeignInvest());
        detail.setLastTwdInvest(book.getLastTwdInvest());

        var currentValue = CalcUtil.multiplyToInt(book.getBalance(), bankBuyingRate);
        detail.setTwdCurrentValue(currentValue);

        if (book.getBalance() > 0) {
            var profit = currentValue - book.getRemainingTwdFund();
            var profitRate = CalcUtil.divideToDouble(profit, book.getRemainingTwdFund(), 4);
            detail.setTwdProfit(profit);
            detail.setTwdProfitRate(profitRate);
        }

        if (detail.getLastTwdInvest() != null && detail.getLastForeignInvest() != null) {
            var lastSellingRate = CalcUtil.divideToDouble(detail.getLastTwdInvest(), detail.getLastForeignInvest(), 4);
            detail.setLastSellingRate(lastSellingRate);
        }

        return detail;
    }

    public static int calcCorrespondTwdFund(Book book, int foreignAmount) {
        Objects.requireNonNull(book);

        if (foreignAmount < 0) {
            throw new IllegalArgumentException("The representing foreign amount shouldn't be negative.");
        }

        if (foreignAmount > book.getBalance()) {
            throw new IllegalArgumentException("The representing foreign amount shouldn't be greater than book balance");
        }

        return CalcUtil.divideToInt(
                CalcUtil.multiplyToDecimal(book.getRemainingTwdFund(), foreignAmount),
                book.getBalance()
        );
    }
}
