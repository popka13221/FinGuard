package com.myname.finguard.common.controller;

import com.myname.finguard.common.dto.CurrencyDto;
import com.myname.finguard.common.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currencies")
@Tag(name = "Lookup", description = "Reference data and helpers")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    @Operation(summary = "Supported currencies", description = "Returns a list of currency codes and names available at registration.")
    @ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<List<CurrencyDto>> list() {
        return ResponseEntity.ok(currencyService.supportedCurrencies());
    }
}
