package com.yourname.finguard.common.controller;

import com.yourname.finguard.common.dto.CurrencyDto;
import com.yourname.finguard.common.service.CurrencyService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public ResponseEntity<List<CurrencyDto>> list() {
        return ResponseEntity.ok(currencyService.supportedCurrencies());
    }
}
