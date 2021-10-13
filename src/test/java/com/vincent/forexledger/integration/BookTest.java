package com.vincent.forexledger.integration;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class BookTest extends BaseTest {

    @Test
    public void testCreateBook() throws Exception {
        var userId = ObjectId.get().toString();
        appendAccessToken(userId, "Vincent");

        var request = new CreateBookRequest();
        request.setName("Book Name");
        request.setBank(BankType.FUBON);
        request.setCurrencyType(CurrencyType.USD);

        var mvcResult = mockMvc.perform(post(APIPathConstants.BOOKS)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        var location = mvcResult.getResponse().getHeader(HttpHeaders.LOCATION);
        var bookId = StringUtils.substringAfterLast(location, '/');

        var actualBook = bookRepository.findById(bookId)
                .orElseThrow(RuntimeException::new);

        Assert.assertEquals(request.getName(), actualBook.getName());
        Assert.assertEquals(request.getBank(), actualBook.getBank());
        Assert.assertEquals(request.getCurrencyType(), actualBook.getCurrencyType());
        Assert.assertEquals(userId, actualBook.getCreator());
        Assert.assertNotNull(actualBook.getCreatedTime());
    }

    @Test
    public void testLoadMyBooks() throws Exception {
        var doraUserId = ObjectId.get().toString();
        var vincentUserId = ObjectId.get().toString();

        appendAccessToken(doraUserId, "Dora");
        createBook("Fubon JPY", BankType.FUBON, CurrencyType.JPY);

        appendAccessToken(vincentUserId, "Vincent");
        var vincentBookIds = List.of(
                createBook("Fubon USD", BankType.FUBON, CurrencyType.USD),
                createBook("Richart EUR", BankType.RICHART, CurrencyType.EUR));

        var mvcResult = mockMvc.perform(get(APIPathConstants.BOOKS)
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn();
        var responseStr = mvcResult.getResponse().getContentAsString();
        BookListResponse[] responses = objectMapper.readValue(responseStr, BookListResponse[].class);

        var responseIds = Arrays.stream(responses)
                .map(BookListResponse::getId)
                .collect(Collectors.toList());
        Assert.assertTrue(CollectionUtils.isEqualCollection(
                vincentBookIds, responseIds));
    }
}
