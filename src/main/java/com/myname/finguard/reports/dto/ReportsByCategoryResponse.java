package com.myname.finguard.reports.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReportsByCategoryResponse(
        String baseCurrency,
        Instant from,
        Instant to,
        List<CategoryTotal> expenses,
        List<CategoryTotal> incomes
) {
    public record CategoryTotal(Long categoryId, String categoryName, BigDecimal total) {
    }
}

