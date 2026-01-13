package com.myname.finguard.common.controller;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.dto.FxRatesResponse;
import com.myname.finguard.common.dto.FxRatesUpsertRequest;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.common.service.FxRatesProvider;
import com.myname.finguard.fx.service.FxRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fx")
@Tag(name = "FX", description = "FX rates")
public class FxController {

    private final CurrencyService currencyService;
    private final FxRateService fxRateService;

    public FxController(CurrencyService currencyService, FxRateService fxRateService) {
        this.currencyService = currencyService;
        this.fxRateService = fxRateService;
    }

    @GetMapping("/rates")
    @Operation(summary = "Latest FX rates", description = "Returns rates for the base currency. "
            + "The quote parameter limits which currencies are returned.")
    @ApiResponse(responseCode = "200", description = "Rates returned")
    public ResponseEntity<FxRatesResponse> latestRates(
            @RequestParam String base,
            @RequestParam(required = false) List<String> quote
    ) {
        FxRatesProvider.FxRates rates = currencyService.latestRates(base);
        Map<String, BigDecimal> filtered = filterQuotes(rates, quote);
        return ResponseEntity.ok(new FxRatesResponse(rates.baseCurrency(), rates.asOf(), filtered));
    }

    @PostMapping("/rates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upsert FX rates", description = "Stores FX rates snapshot for the base currency (admin only).")
    @ApiResponse(responseCode = "200", description = "Rates stored")
    public ResponseEntity<FxRatesResponse> upsertRates(@Valid @RequestBody FxRatesUpsertRequest request) {
        FxRatesProvider.FxRates saved = fxRateService.upsertSnapshot(request.baseCurrency(), request.asOf(), request.rates());
        return ResponseEntity.ok(new FxRatesResponse(saved.baseCurrency(), saved.asOf(), saved.rates()));
    }

    private Map<String, BigDecimal> filterQuotes(FxRatesProvider.FxRates rates, List<String> quotes) {
        if (quotes == null || quotes.isEmpty()) {
            return rates.rates();
        }
        Map<String, BigDecimal> filtered = new LinkedHashMap<>();
        for (String raw : quotes) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String normalized = currencyService.normalize(raw);
            if (!currencyService.isSupported(normalized)) {
                throw new ApiException(
                        ErrorCodes.BAD_REQUEST,
                        "Unsupported quote currency: " + normalized,
                        HttpStatus.BAD_REQUEST
                );
            }
            if (normalized.equalsIgnoreCase(rates.baseCurrency())) {
                filtered.put(rates.baseCurrency(), BigDecimal.ONE);
                continue;
            }
            BigDecimal value = rates.rates().get(normalized);
            if (value == null) {
                throw new ApiException(
                        ErrorCodes.BAD_REQUEST,
                        "FX rate is not available for quote currency: " + normalized,
                        HttpStatus.BAD_REQUEST
                );
            }
            filtered.put(normalized, value);
        }
        return filtered.isEmpty() ? rates.rates() : filtered;
    }
}
