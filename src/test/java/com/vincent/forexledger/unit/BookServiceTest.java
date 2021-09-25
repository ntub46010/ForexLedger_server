package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.Book;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.repository.BookRepository;
import com.vincent.forexledger.security.UserIdentity;
import com.vincent.forexledger.service.BookService;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

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
    }
}
