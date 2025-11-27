package com.yourname.finguard.common.service;

import com.yourname.finguard.common.dto.CurrencyDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {

    private static final List<CurrencyDto> SUPPORTED = List.of(
            new CurrencyDto("USD", "US Dollar"),
            new CurrencyDto("EUR", "Euro"),
            new CurrencyDto("RUB", "Russian Ruble"),
            new CurrencyDto("BTC", "Bitcoin"),
            new CurrencyDto("ETH", "Ethereum")
    );

    public List<CurrencyDto> supportedCurrencies() {
        return SUPPORTED;
    }

    public String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    public boolean isSupported(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String normalized = normalize(code);
        return SUPPORTED.stream().anyMatch(c -> c.code().equalsIgnoreCase(normalized));
    }
}
