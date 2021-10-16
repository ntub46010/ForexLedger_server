package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.service.BookService;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    @Test
    public void testCreateBook() {
        var userId = ObjectId.get().toString();
        var bookId = ObjectId.get().toString();

        var userIdentity = mock(UserIdentity.class);
        var repository = mock(BookRepository.class);
        var service = new BookService(userIdentity, repository);

        when(userIdentity.getId()).thenReturn(userId);
        when(repository.insert(any(Book.class)))
                .then((Answer<Book>) invocationOnMock -> {
                    var book = (Book) invocationOnMock.getArgument(0);
                    book.setId(bookId);
                    return book;
                });

        var request = new CreateBookRequest();
        request.setName("Book Name");
        request.setBank(BankType.RICHART);
        request.setCurrencyType(CurrencyType.JPY);
        var createdBookId = service.createBook(request);

        verify(userIdentity).getId();

        var insertBookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(repository).insert(insertBookCaptor.capture());

        var actualBook = insertBookCaptor.getValue();
        Assert.assertNotNull(createdBookId);
        Assert.assertEquals(request.getName(), actualBook.getName());
        Assert.assertEquals(request.getBank(), actualBook.getBank());
        Assert.assertEquals(request.getCurrencyType(), actualBook.getCurrencyType());
        Assert.assertEquals(userId, actualBook.getCreator());
        Assert.assertNotNull(actualBook.getCreatedTime());
        Assert.assertEquals(0, actualBook.getBalance(), 0);
        Assert.assertNull(actualBook.getTwdProfit());
        Assert.assertNull(actualBook.getProfitRate());
        Assert.assertNull(actualBook.getBreakEvenPoint());
        Assert.assertNull(actualBook.getLastForeignInvest());
        Assert.assertNull(actualBook.getLastTwdInvest());
    }

    @Test
    public void testLoadMyBooks() {
        var userId1 = ObjectId.get().toString();
        var userId2 = ObjectId.get().toString();

        var books1 = List.of(
                createBook(userId1),
                createBook(userId1));
        var books2 = List.of(
                createBook(userId2),
                createBook(userId2));

        var userIdentity = mock(UserIdentity.class);
        var repository = mock(BookRepository.class);
        var service = new BookService(userIdentity, repository);

        when(userIdentity.getId()).thenReturn(userId1);
        when(repository.findByCreator(userId1)).thenReturn(books1);
        when(repository.findByCreator(userId2)).thenReturn(books2);

        var responses = service.loadMyBooks();

        verify(repository).findByCreator(userId1);
        verify(repository, times(0)).findByCreator(userId2);

        var bookIds1 = books1.stream()
                .map(Book::getId)
                .collect(Collectors.toList());
        var responseIds = responses.stream()
                .map(BookListResponse::getId)
                .collect(Collectors.toList());
        Assert.assertTrue(CollectionUtils.isEqualCollection(bookIds1, responseIds));
    }

    private Book createBook(String creator) {
        var book = new Book();
        book.setId(ObjectId.get().toString());
        book.setName("Book Name");
        book.setCurrencyType(CurrencyType.USD);
        book.setBalance(1947.33);
        book.setTwdProfit(-3671);
        book.setProfitRate(-0.0189);
        book.setCreator(creator);

        return book;
    }
}
