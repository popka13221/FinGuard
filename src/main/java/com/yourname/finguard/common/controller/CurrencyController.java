package com.yourname.finguard.common.controller;

import com.yourname.finguard.common.dto.CurrencyDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    @GetMapping
    public ResponseEntity<List<CurrencyDto>> list() {
        return ResponseEntity.ok(List.of(
                new CurrencyDto("USD", "US Dollar"),
                new CurrencyDto("EUR", "Euro"),
                new CurrencyDto("RUB", "Russian Ruble"),
                new CurrencyDto("BTC", "Bitcoin"),
                new CurrencyDto("ETH", "Ethereum")
        ));
    }
}
