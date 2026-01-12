package com.myname.finguard.categories.dto;

import com.myname.finguard.categories.model.CategoryType;

public record CategoryDto(
        Long id,
        String name,
        CategoryType type,
        boolean system
) {
}

