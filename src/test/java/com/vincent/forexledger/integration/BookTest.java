package com.vincent.forexledger.integration;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.BookListResponse;
import com.vincent.forexledger.model.book.CreateBookRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.hamcrest.core.IsNull;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

        // TODO: test profit value
    }

    @Test
    public void testLoadEmptyBookDetail() throws Exception {
        var userId = ObjectId.get().toString();
        appendAccessToken(userId, "Vincent");

        var exchangeRate = exchangeRateTable.get(BankType.FUBON, CurrencyType.USD);
        var bookId = createBook("Book Name", BankType.FUBON, CurrencyType.USD);

        mockMvc.perform(get(APIPathConstants.BOOKS + "/" + bookId)
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.currencyType").value(exchangeRate.getCurrencyType().name()))
                .andExpect(jsonPath("$.bankBuyingRate").value(exchangeRate.getBuyingRate()))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.twdCurrentValue").value(0))
                .andExpect(jsonPath("$.twdProfit").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.twdProfitRate").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.breakEvenPoint").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.lastForeignInvest").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.lastTwdInvest").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.lastSellingRate").value(IsNull.nullValue()));
    }

    @Test
    public void testLoadNotEmptyBookDetail() throws Exception {
        var userId = ObjectId.get().toString();
        appendAccessToken(userId, "Vincent");

        var exchangeRate = exchangeRateTable.get(BankType.FUBON, CurrencyType.GBP);

        var bookId = createBook("Book Name", BankType.FUBON, CurrencyType.GBP);
        var book = bookRepository.findById(bookId).orElseThrow();

        book.setBalance(621.77);
        book.setRemainingTwdFund(23877);
        book.setBreakEvenPoint(38.421);
        book.setLastForeignInvest(78.44);
        book.setLastTwdInvest(3000);
        bookRepository.save(book);

        mockMvc.perform(get(APIPathConstants.BOOKS + "/" + bookId)
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.currencyType").value(exchangeRate.getCurrencyType().name()))
                .andExpect(jsonPath("$.bankBuyingRate").value(exchangeRate.getBuyingRate()))
                .andExpect(jsonPath("$.balance").value(book.getBalance()))
                .andExpect(jsonPath("$.twdCurrentValue").value(23738))
                .andExpect(jsonPath("$.twdProfit").value(-139))
                .andExpect(jsonPath("$.twdProfitRate").value(-0.0058))
                .andExpect(jsonPath("$.breakEvenPoint").value(book.getBreakEvenPoint()))
                .andExpect(jsonPath("$.lastForeignInvest").value(book.getLastForeignInvest()))
                .andExpect(jsonPath("$.lastTwdInvest").value(book.getLastTwdInvest()))
                .andExpect(jsonPath("$.lastSellingRate").value(38.2458));

    }

}
