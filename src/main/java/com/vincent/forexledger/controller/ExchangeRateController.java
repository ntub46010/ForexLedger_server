package com.vincent.forexledger.controller;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.constants.QueryStringConstants;
import com.vincent.forexledger.model.bank.BankType;
import com.vincent.forexledger.model.exchangerate.ExchangeRateResponse;
import com.vincent.forexledger.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = APIPathConstants.EXCHANGE_RATES, produces = MediaType.APPLICATION_JSON_VALUE)
public class ExchangeRateController {

    @Autowired
    private ExchangeRateService service;

    @Operation(
            summary = "Get exchange rates of specific bank.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Get exchange rates successfully."),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Can't find exchange rates of specific bank.",
                            content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<List<ExchangeRateResponse>> getExchangeRates(
            @Parameter(description = "Bank of exchange rates.", required = true)
            @RequestParam(QueryStringConstants.BANK) BankType bank) {
        List<ExchangeRateResponse> responses = service.loadExchangeRates(bank);
        return ResponseEntity.ok(responses);
    }
}
