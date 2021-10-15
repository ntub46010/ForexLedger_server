package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.book.BookDetailResponse;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.util.CalcUtil;

import java.util.List;
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
        book.setTwdProfit(null);
        book.setProfitRate(null);

        return book;
    }

    public static List<BookListResponse> toBookListResponses(List<Book> books) {
        return books.stream()
                .map(BookConverter::toBookListResponse)
                .collect(Collectors.toList());
    }

    public static BookListResponse toBookListResponse(Book book) {
        var response = new BookListResponse();
        response.setId(book.getId());
        response.setName(book.getName());
        response.setCurrencyType(book.getCurrencyType());
        response.setBalance(book.getBalance());
        response.setTwdProfit(book.getTwdProfit());
        response.setProfitRate(book.getProfitRate());

        return response;
    }

    public static BookDetailResponse toBookDetail(Book book, ExchangeRate exchangeRate) {
        var detail = new BookDetailResponse();
        detail.setId(book.getId());
        detail.setCurrencyType(book.getCurrencyType());
        detail.setBankSellingRate(exchangeRate.getSellingRate());
        detail.setBankBuyingRate(exchangeRate.getBuyingRate());
        detail.setBalance(book.getBalance());
        detail.setTwdCurrentValue(CalcUtil.multiplyToInt(book.getBalance(), exchangeRate.getBuyingRate()));
//        detail.setTwdProfit(0);
//        detail.setTwdProfitRate(0);

        if (book.getBalance() > 0) {
//        detail.setBreakEvenPoint(null);
        }

        detail.setForeignLastInvest(null);
        detail.setTwdLastInvest(null);

        return detail;
    }
}
