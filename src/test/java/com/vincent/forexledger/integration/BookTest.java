package com.vincent.forexledger.integration;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.book.CreateBookRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class BookTest extends BaseTest {

    @Test
    public void testCreateBook() throws Exception {
        var request = new CreateBookRequest();
        request.setName("Book Name");
        request.setBank(BankType.FUBON);
        request.setCurrencyType(CurrencyType.USD);

        // TODO: token

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
        Assert.assertEquals("", actualBook.getCreator());  // TODO
        Assert.assertNotNull(actualBook.getCreatedTime());
    }
}
