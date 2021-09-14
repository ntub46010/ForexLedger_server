package com.vincent.forexledger.unit;

import com.vincent.forexledger.client.DownloadExchangeRateClient;
import com.vincent.forexledger.model.CurrencyType;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRate;
import com.vincent.forexledger.model.exchangerate.FindRateResponse;
import com.vincent.forexledger.repository.ExchangeRateRepository;
import com.vincent.forexledger.service.ExchangeRateService;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExchangeRateServiceTests {
    private DownloadExchangeRateClient client;
    private ExchangeRateRepository repository;
    private ExchangeRateService service;

    @Before
    public void setup() {
        client = mock(DownloadExchangeRateClient.class);
        repository = mock(ExchangeRateRepository.class);
        service = new ExchangeRateService(client, repository);
    }

    @Test
    public void testRefreshExchangeRateOf2Banks() {
        List<FindRateResponse> fubonFindRateResponses = createFakeFindRateResponse(BankType.FUBON,
                CurrencyType.USD, CurrencyType.CNY, CurrencyType.JPY);
        List<FindRateResponse> richartFindRateResponses = createFakeFindRateResponse(BankType.RICHART,
                CurrencyType.USD, CurrencyType.EUR, CurrencyType.HKD);
        List<FindRateResponse> allFindRateResponse = Stream.of(fubonFindRateResponses, richartFindRateResponses)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<ExchangeRate> oldExRates = Arrays.asList(
                createFakeExchangeRate(),
                createFakeExchangeRate(),
                createFakeExchangeRate());

        when(client.load(BankType.FUBON)).thenReturn(fubonFindRateResponses);
        when(client.load(BankType.RICHART)).thenReturn(richartFindRateResponses);
        when(repository.findByBankTypeIn(anyCollection())).thenReturn(oldExRates);

        service.refreshExchangeRateData();

        verify(client).load(BankType.FUBON);
        verify(client).load(BankType.RICHART);

        ArgumentCaptor<List<ExchangeRate>> newExRatesCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).insert(newExRatesCaptor.capture());

        ArgumentCaptor<List<String>> deletedExRateIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).deleteAllById(deletedExRateIdsCaptor.capture());

        List<TestExchangeRate> expectedSavedRates = allFindRateResponse.stream()
                .map(TestExchangeRate::new)
                .collect(Collectors.toList());
        Map<BankType, List<CurrencyType>> expectedSavedRateMap = groupBankToCurrencyTypesMap(expectedSavedRates);

        List<TestExchangeRate> actualSavedRates = newExRatesCaptor.getValue().stream()
                .map(TestExchangeRate::new)
                .collect(Collectors.toList());
        Map<BankType, List<CurrencyType>> actualSavedRateMap = groupBankToCurrencyTypesMap(actualSavedRates);

        Assert.assertTrue(CollectionUtils.isEqualCollection(
                expectedSavedRateMap.keySet(), actualSavedRateMap.keySet()));
        expectedSavedRateMap.forEach((bank, expectedCurrencyTypes) -> {
            List<CurrencyType> actualCurrencyType = actualSavedRateMap.get(bank);
            Assert.assertTrue(CollectionUtils.isEqualCollection(
                    actualCurrencyType, expectedCurrencyTypes));
        });

        List<String> oldExRateIds = oldExRates.stream()
                .map(ExchangeRate::getId)
                .collect(Collectors.toList());
        Assert.assertTrue(CollectionUtils.isEqualCollection(
                deletedExRateIdsCaptor.getValue(), oldExRateIds));
    }

    private List<FindRateResponse> createFakeFindRateResponse(BankType bank, CurrencyType... currencyTypes) {
        return Arrays.stream(currencyTypes)
                .map(currency -> {
                    FindRateResponse rate = new FindRateResponse();
                    rate.setCurrencyType(currency);
                    rate.setBankType(bank);

                    return rate;
                })
                .collect(Collectors.toList());
    }

    private ExchangeRate createFakeExchangeRate() {
        ExchangeRate rate = new ExchangeRate();
        rate.setId(ObjectId.get().toHexString());

        return rate;
    }

    private Map<BankType, List<CurrencyType>> groupBankToCurrencyTypesMap(List<TestExchangeRate> rates) {
        Map<BankType, List<CurrencyType>> resultMap = new EnumMap<>(BankType.class);
        for (TestExchangeRate rate : rates) {
            List<CurrencyType> currencyTypes = resultMap.get(rate.getBankType());
            if (currencyTypes == null) {
                currencyTypes = new ArrayList<>();
            }
            currencyTypes.add(rate.getCurrencyType());
            resultMap.put(rate.getBankType(), currencyTypes);
        }

        return resultMap;
    }

    private class TestExchangeRate {
        private CurrencyType currencyType;
        private BankType bankType;

        TestExchangeRate(ExchangeRate rate) {
            this.currencyType = rate.getCurrencyType();
            this.bankType = rate.getBankType();
        }

        TestExchangeRate(FindRateResponse rate) {
            this.currencyType = rate.getCurrencyType();
            this.bankType = rate.getBankType();
        }

        CurrencyType getCurrencyType() {
            return currencyType;
        }

        BankType getBankType() {
            return bankType;
        }
    }

}
