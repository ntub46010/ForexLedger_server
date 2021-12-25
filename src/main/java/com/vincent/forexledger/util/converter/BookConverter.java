package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.backup.BookAndEntryBackup;
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
            response.setTwdProfit(profit);

            if (book.getRemainingTwdFund() > 0) {
                var profitRate = CalcUtil.divideToDouble(profit, book.getRemainingTwdFund(), 4);
                response.setProfitRate(profitRate);
            }
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
            detail.setTwdProfit(profit);

            if (book.getRemainingTwdFund() > 0) {
                var profitRate = CalcUtil.divideToDouble(profit, book.getRemainingTwdFund(), 4);
                detail.setTwdProfitRate(profitRate);
            }
        }

        if (detail.getLastTwdInvest() != null && detail.getLastForeignInvest() != null) {
            var lastSellingRate = CalcUtil.divideToDouble(detail.getLastTwdInvest(), detail.getLastForeignInvest(), 4);
            detail.setLastSellingRate(lastSellingRate);
        }

        return detail;
    }

    // TODO: unit test
    public static BookAndEntryBackup.BookBackup toBookBackup(Book book) {
        var backup = new BookAndEntryBackup.BookBackup();
        backup.setName(book.getName());
        backup.setBank(book.getBank());
        backup.setCurrencyType(book.getCurrencyType());
        backup.setCreatedTime(book.getCreatedTime());

        return backup;
    }

    public static int calcRepresentingTwdFund(Book book, double foreignAmount) {
        Objects.requireNonNull(book);
        return calcRepresentingTwdFund(book.getRemainingTwdFund(), book.getBalance(), foreignAmount);
    }

    // TODO: unit test
    public static int calcRepresentingTwdFund(int totalTwdFund, double totalBalance, double foreignAmount) {
        if (totalTwdFund == 0) {
            return 0;
        }

        if (foreignAmount < 0) {
            throw new IllegalArgumentException("The representing foreign amount shouldn't be negative.");
        }

        if (foreignAmount > totalBalance) {
            throw new IllegalArgumentException("The representing foreign amount shouldn't be greater than book balance");
        }

        return CalcUtil.divideToInt(
                CalcUtil.multiplyToDecimal(totalTwdFund, foreignAmount),
                totalBalance
        );
    }

}
