package com.vincent.forexledger.integration;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.TransactionType;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class EntryTest extends BaseTest {

    @Test
    public void testBookTransferIn() throws Exception {
        appendAccessToken(ObjectId.get().toString(), "Vincent");
        var bookId = createBook("Test Book", BankType.FUBON, CurrencyType.GBP);

        var entryReq = new CreateEntryRequest();
        entryReq.setBookId(bookId);
        entryReq.setTransactionType(TransactionType.TRANSFER_IN_FROM_TWD);
        entryReq.setTransactionDate(new Date());
        entryReq.setForeignAmount(150);
        entryReq.setTwdAmount(5751);

        var mvcResult = mockMvc.perform(post(APIPathConstants.ENTRIES)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isCreated())
                .andReturn();
        var location = mvcResult.getResponse().getHeader(HttpHeaders.LOCATION);
        var entryId = StringUtils.substringAfterLast(location, '/');

        var entry = entryRepository.findById(entryId).orElseThrow();
        Assert.assertEquals(entry.getBookId(), entryReq.getBookId());
        Assert.assertEquals(entry.getTransactionType(), entryReq.getTransactionType());
        Assert.assertEquals(entry.getTransactionDate(), entryReq.getTransactionDate());
        Assert.assertEquals(entry.getForeignAmount(), entryReq.getForeignAmount(), 0);
        Assert.assertEquals(entry.getTwdAmount(), entryReq.getTwdAmount());
        Assert.assertNull(entry.getRelatedBookId());
        Assert.assertNull(entry.getRelatedForeignAmount());

        var book = bookRepository.findById(bookId).orElseThrow();
        Assert.assertEquals(entryReq.getForeignAmount(), book.getBalance(), 0);
        Assert.assertEquals((int) entryReq.getTwdAmount(), book.getRemainingTwdFund());
        Assert.assertEquals(38.34, book.getBreakEvenPoint(), 0);
        Assert.assertEquals(entryReq.getForeignAmount(), book.getLastForeignInvest(), 0);
        Assert.assertEquals(entryReq.getTwdAmount(), book.getLastTwdInvest());
    }
}
