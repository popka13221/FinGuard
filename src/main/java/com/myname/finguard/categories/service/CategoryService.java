package com.myname.finguard.categories.service;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.categories.dto.CategoryDto;
import com.myname.finguard.categories.dto.CreateCategoryRequest;
import com.myname.finguard.categories.dto.UpdateCategoryRequest;
import com.myname.finguard.categories.model.Category;
import com.myname.finguard.categories.model.CategoryType;
import com.myname.finguard.categories.repository.CategoryRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.transactions.repository.TransactionRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<CategoryDto> listCategories(Long userId) {
        if (userId == null) {
            throw unauthorized();
        }
        return categoryRepository.findByUserIdOrUserIsNull(userId).stream()
                .map(this::toDto)
                .sorted(Comparator
                        .comparing(CategoryDto::system)
                        .thenComparing(CategoryDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public CategoryDto createCategory(Long userId, CreateCategoryRequest request) {
        if (userId == null) {
            throw unauthorized();
        }
        if (request == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Request body is required", HttpStatus.BAD_REQUEST);
        }
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isBlank()) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Category name is required", HttpStatus.BAD_REQUEST);
        }
        if (name.length() > 255) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Category name must be at most 255 characters", HttpStatus.BAD_REQUEST);
        }
        CategoryType type = request.type() == null ? null : request.type();
        if (type == null) {
            throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Category type is required", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setType(type);
        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }

    public CategoryDto updateCategory(Long userId, Long categoryId, UpdateCategoryRequest request) {
        if (userId == null) {
            throw unauthorized();
        }
        if (categoryId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category id is required", HttpStatus.BAD_REQUEST);
        }
        if (request == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Request body is required", HttpStatus.BAD_REQUEST);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Category not found", HttpStatus.BAD_REQUEST));
        if (category.getUser() == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "System categories cannot be updated", HttpStatus.BAD_REQUEST);
        }
        if (category.getUser().getId() == null || !category.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category not found", HttpStatus.BAD_REQUEST);
        }

        if (request.name() != null) {
            String name = request.name().trim();
            if (name.isBlank()) {
                throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Category name is required", HttpStatus.BAD_REQUEST);
            }
            if (name.length() > 255) {
                throw new ApiException(ErrorCodes.VALIDATION_GENERIC, "Category name must be at most 255 characters", HttpStatus.BAD_REQUEST);
            }
            category.setName(name);
        }

        if (request.type() != null) {
            category.setType(request.type());
        }

        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }

    public void deleteCategory(Long userId, Long categoryId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (categoryId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category id is required", HttpStatus.BAD_REQUEST);
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Category not found", HttpStatus.BAD_REQUEST));
        if (category.getUser() == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "System categories cannot be deleted", HttpStatus.BAD_REQUEST);
        }
        if (category.getUser().getId() == null || !category.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category not found", HttpStatus.BAD_REQUEST);
        }
        if (transactionRepository != null && transactionRepository.countByCategoryIdAndUserId(categoryId, userId) > 0) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category has transactions and cannot be deleted", HttpStatus.BAD_REQUEST);
        }
        categoryRepository.delete(category);
    }

    public boolean isCategoryCompatible(Category category, com.myname.finguard.transactions.model.TransactionType transactionType) {
        if (category == null || category.getType() == null || transactionType == null) {
            return false;
        }
        CategoryType type = category.getType();
        return switch (transactionType) {
            case INCOME -> type == CategoryType.INCOME || type == CategoryType.BOTH;
            case EXPENSE -> type == CategoryType.EXPENSE || type == CategoryType.BOTH;
        };
    }

    public Category requireAccessibleCategory(Long userId, Long categoryId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (categoryId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category id is required", HttpStatus.BAD_REQUEST);
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Category not found", HttpStatus.BAD_REQUEST));
        if (category.getUser() == null) {
            return category;
        }
        if (category.getUser().getId() == null || !category.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category not found", HttpStatus.BAD_REQUEST);
        }
        return category;
    }

    private CategoryDto toDto(Category category) {
        boolean system = category.getUser() == null;
        return new CategoryDto(category.getId(), category.getName(), category.getType(), system);
    }

    private ApiException unauthorized() {
        return new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User is not authenticated", HttpStatus.UNAUTHORIZED);
    }
}

