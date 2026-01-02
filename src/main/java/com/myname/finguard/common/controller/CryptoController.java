package com.myname.finguard.common.controller;

import com.myname.finguard.common.dto.CryptoRateDto;
import com.myname.finguard.common.dto.CryptoRatesResponse;
import com.myname.finguard.common.service.CryptoRatesProvider;
import com.myname.finguard.common.service.CryptoRatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crypto")
@Tag(name = "Crypto", description = "Crypto market rates")
public class CryptoController {

    private final CryptoRatesService cryptoRatesService;

    public CryptoController(CryptoRatesService cryptoRatesService) {
        this.cryptoRatesService = cryptoRatesService;
    }

    @GetMapping("/rates")
    @Operation(summary = "Latest crypto rates", description = "Returns latest crypto prices with a 7d sparkline.")
    @ApiResponse(responseCode = "200", description = "Rates returned")
    public ResponseEntity<CryptoRatesResponse> latestRates(@RequestParam(required = false) String base) {
        CryptoRatesProvider.CryptoRates rates = cryptoRatesService.latestRates(base);
        List<CryptoRateDto> items = rates.rates().stream()
                .map(rate -> new CryptoRateDto(
                        rate.code(),
                        rate.name(),
                        rate.price(),
                        rate.changePct24h(),
                        rate.sparkline()
                ))
                .toList();
        return ResponseEntity.ok(new CryptoRatesResponse(rates.baseCurrency(), rates.asOf(), items));
    }
}
