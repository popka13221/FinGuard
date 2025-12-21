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
@Tag(name = "Lookup", description = "Справочники и вспомогательные данные")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    @Operation(summary = "Список поддерживаемых валют", description = "Возвращает список кодов/названий валют, которые можно выбрать при регистрации")
    @ApiResponse(responseCode = "200", description = "Успешный ответ")
    public ResponseEntity<List<CurrencyDto>> list() {
        return ResponseEntity.ok(currencyService.supportedCurrencies());
    }
}
