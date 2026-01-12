package com.myname.finguard.categories.dto;

import com.myname.finguard.categories.model.CategoryType;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @Size(max = 255, message = "Category name must be at most 255 characters")
        String name,
        CategoryType type
) {
}

