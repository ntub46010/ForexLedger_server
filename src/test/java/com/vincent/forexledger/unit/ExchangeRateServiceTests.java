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
    @SuppressWarnings("unchecked")
    public void testRefreshExchangeRateOf2Banks() {
        var fubonFindRateResponses = createFakeFindRateResponse(BankType.FUBON,
                CurrencyType.USD, CurrencyType.CNY, CurrencyType.JPY);
        var richartFindRateResponses = createFakeFindRateResponse(BankType.RICHART,
                CurrencyType.USD, CurrencyType.EUR, CurrencyType.HKD);
        var allFindRateResponse = Stream.of(fubonFindRateResponses, richartFindRateResponses)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        var fubonOldExRates = Arrays.asList(
                createFakeExchangeRate(BankType.FUBON),
                createFakeExchangeRate(BankType.FUBON));
        var richartOldExRates = Arrays.asList(
                createFakeExchangeRate(BankType.RICHART),
                createFakeExchangeRate(BankType.RICHART));
        var allOldExRates = Stream.of(fubonOldExRates, richartOldExRates)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        when(client.load(BankType.FUBON)).thenReturn(fubonFindRateResponses);
        when(client.load(BankType.RICHART)).thenReturn(richartFindRateResponses);
        when(repository.findByBankTypeIn(Set.of(BankType.FUBON, BankType.RICHART)))
                .thenReturn(allOldExRates);

        service.refreshExchangeRateData();

        verify(client).load(BankType.FUBON);
        verify(client).load(BankType.RICHART);

        ArgumentCaptor<List<ExchangeRate>> newExRatesCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).insert(newExRatesCaptor.capture());

        ArgumentCaptor<List<String>> deletedExRateIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).deleteAllById(deletedExRateIdsCaptor.capture());

        var expectedSavedRates = allFindRateResponse.stream()
                .map(TestExchangeRate::new)
                .collect(Collectors.toList());
        var expectedBankToSavedCurrencyTypesMap = createBankToCurrencyTypesMap(expectedSavedRates);

        var actualSavedRates = newExRatesCaptor.getValue().stream()
                .map(TestExchangeRate::new)
                .collect(Collectors.toList());
        var actualBankToSavedCurrencyTypesMap = createBankToCurrencyTypesMap(actualSavedRates);

        Assert.assertTrue(CollectionUtils.isEqualCollection(
                expectedBankToSavedCurrencyTypesMap.keySet(), actualBankToSavedCurrencyTypesMap.keySet()));
        expectedBankToSavedCurrencyTypesMap.forEach((bank, expectedCurrencyTypes) -> {
            List<CurrencyType> actualCurrencyType = actualBankToSavedCurrencyTypesMap.get(bank);
            Assert.assertTrue(CollectionUtils.isEqualCollection(
                    actualCurrencyType, expectedCurrencyTypes));
        });

        var oldExRateIds = allOldExRates.stream()
                .map(ExchangeRate::getId)
                .collect(Collectors.toList());
        Assert.assertTrue(CollectionUtils.isEqualCollection(
                deletedExRateIdsCaptor.getValue(), oldExRateIds));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRefreshExchangeRateBut1BankFailed() {
        var fubonFindRateResponses = createFakeFindRateResponse(BankType.FUBON,
                CurrencyType.USD, CurrencyType.CNY, CurrencyType.JPY);

        when(client.load(BankType.FUBON)).thenReturn(fubonFindRateResponses);
        when(client.load(BankType.RICHART)).thenThrow(new RuntimeException());

        var fubonOldExRates = Arrays.asList(
                createFakeExchangeRate(BankType.FUBON),
                createFakeExchangeRate(BankType.FUBON));
        when(repository.findByBankTypeIn(Set.of(BankType.FUBON)))
                .thenReturn(fubonOldExRates);

        service.refreshExchangeRateData();

        verify(client).load(BankType.FUBON);
        verify(client).load(BankType.RICHART);

        ArgumentCaptor<List<ExchangeRate>> newExRatesCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).insert(newExRatesCaptor.capture());

        ArgumentCaptor<List<String>> deletedExRateIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).deleteAllById(deletedExRateIdsCaptor.capture());

        var actualSavedExRates = newExRatesCaptor.getValue();
        actualSavedExRates.forEach(rate -> Assert.assertEquals(BankType.FUBON, rate.getBankType()));
        var expectedSavedCurrencyTypes = fubonFindRateResponses.stream()
                .map(FindRateResponse::getCurrencyType)
                .collect(Collectors.toList());
        var actualSavedCurrencyTypes = actualSavedExRates.stream()
                .map(ExchangeRate::getCurrencyType)
                .collect(Collectors.toList());
        Assert.assertTrue(CollectionUtils.isEqualCollection(
                expectedSavedCurrencyTypes, actualSavedCurrencyTypes));

        var fubonOldExRateIds = fubonOldExRates.stream()
                .map(ExchangeRate::getId)
                .collect(Collectors.toList());
        Assert.assertTrue(CollectionUtils.isEqualCollection(
                deletedExRateIdsCaptor.getValue(), fubonOldExRateIds));
    }

    private List<FindRateResponse> createFakeFindRateResponse(BankType bank, CurrencyType... currencyTypes) {
        return Arrays.stream(currencyTypes)
                .map(currency -> {
                    var rate = new FindRateResponse();
                    rate.setCurrencyType(currency);
                    rate.setBankType(bank);

                    return rate;
                })
                .collect(Collectors.toList());
    }

    private ExchangeRate createFakeExchangeRate(BankType bank) {
        var rate = new ExchangeRate();
        rate.setId(ObjectId.get().toHexString());
        rate.setBankType(bank);

        return rate;
    }

    private Map<BankType, List<CurrencyType>> createBankToCurrencyTypesMap(List<TestExchangeRate> rates) {
        var resultMap = new EnumMap<BankType, List<CurrencyType>>(BankType.class);
        for (var rate : rates) {
            var currencyTypes = resultMap.get(rate.getBankType());
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
