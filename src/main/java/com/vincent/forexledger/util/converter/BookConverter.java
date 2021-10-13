package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;

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
}
