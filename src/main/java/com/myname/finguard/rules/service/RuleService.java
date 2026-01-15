package com.myname.finguard.rules.service;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.categories.model.Category;
import com.myname.finguard.categories.service.CategoryService;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.common.service.CurrencyService;
import com.myname.finguard.notifications.repository.NotificationRepository;
import com.myname.finguard.rules.dto.CreateRuleRequest;
import com.myname.finguard.rules.dto.RuleDto;
import com.myname.finguard.rules.dto.UpdateRuleRequest;
import com.myname.finguard.rules.model.Rule;
import com.myname.finguard.rules.model.RuleStatus;
import com.myname.finguard.rules.model.RuleType;
import com.myname.finguard.rules.repository.RuleRepository;
import com.myname.finguard.transactions.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleService {

    private final RuleRepository ruleRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final CurrencyService currencyService;
    private final RuleParamsCodec ruleParamsCodec;
    private final NotificationRepository notificationRepository;

    public RuleService(
            RuleRepository ruleRepository,
            UserRepository userRepository,
            CategoryService categoryService,
            CurrencyService currencyService,
            RuleParamsCodec ruleParamsCodec,
            NotificationRepository notificationRepository
    ) {
        this.ruleRepository = ruleRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.currencyService = currencyService;
        this.ruleParamsCodec = ruleParamsCodec;
        this.notificationRepository = notificationRepository;
    }

    public List<RuleDto> listRules(Long userId) {
        if (userId == null) {
            throw unauthorized();
        }
        return ruleRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public RuleDto getRule(Long userId, Long ruleId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (ruleId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Rule id is required", HttpStatus.BAD_REQUEST);
        }
        Rule rule = ruleRepository.findByIdAndUserId(ruleId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Rule not found", HttpStatus.BAD_REQUEST));
        return toDto(rule);
    }

    public RuleDto createRule(Long userId, CreateRuleRequest request) {
        if (userId == null) {
            throw unauthorized();
        }
        if (request == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Request body is required", HttpStatus.BAD_REQUEST);
        }
        RuleType type = requireType(request.type());
        if (type != RuleType.SPENDING_LIMIT_CATEGORY_MONTHLY) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported rule type", HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);

        String currency = resolveCurrency(request.currency(), user.getBaseCurrency(), true);
        SpendingLimitParams params = buildSpendingLimitParams(userId, request.categoryId(), request.limit(), currency);

        Instant now = Instant.now();
        Rule rule = new Rule();
        rule.setUser(user);
        rule.setType(type);
        rule.setStatus(resolveStatus(request.active()));
        rule.setParamsJson(ruleParamsCodec.encode(params));
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);

        Rule saved = ruleRepository.save(rule);
        return toDto(saved);
    }

    public RuleDto updateRule(Long userId, Long ruleId, UpdateRuleRequest request) {
        if (userId == null) {
            throw unauthorized();
        }
        if (ruleId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Rule id is required", HttpStatus.BAD_REQUEST);
        }
        if (request == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Request body is required", HttpStatus.BAD_REQUEST);
        }
        Rule rule = ruleRepository.findByIdAndUserId(ruleId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Rule not found", HttpStatus.BAD_REQUEST));
        SpendingLimitParams current = ruleParamsCodec.decode(rule.getParamsJson());

        Long nextCategoryId = request.categoryId() != null ? request.categoryId() : current.categoryId();
        BigDecimal nextLimit = request.limit() != null ? request.limit() : current.limit();
        String nextCurrency = current.currency();
        if (request.currency() != null) {
            nextCurrency = resolveCurrency(request.currency(), null, false);
        }

        SpendingLimitParams nextParams = buildSpendingLimitParams(userId, nextCategoryId, nextLimit, nextCurrency);
        rule.setParamsJson(ruleParamsCodec.encode(nextParams));

        if (request.active() != null) {
            rule.setStatus(resolveStatus(request.active()));
        }

        rule.setUpdatedAt(Instant.now());
        Rule saved = ruleRepository.save(rule);
        return toDto(saved);
    }

    @Transactional
    public void deleteRule(Long userId, Long ruleId) {
        if (userId == null) {
            throw unauthorized();
        }
        if (ruleId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Rule id is required", HttpStatus.BAD_REQUEST);
        }
        Rule rule = ruleRepository.findByIdAndUserId(ruleId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Rule not found", HttpStatus.BAD_REQUEST));
        notificationRepository.deleteByRuleId(rule.getId());
        ruleRepository.delete(rule);
    }

    private SpendingLimitParams buildSpendingLimitParams(Long userId, Long categoryId, BigDecimal limit, String currency) {
        if (categoryId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category id is required", HttpStatus.BAD_REQUEST);
        }
        Category category = categoryService.requireAccessibleCategory(userId, categoryId);
        if (!categoryService.isCategoryCompatible(category, TransactionType.EXPENSE)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Category type is not compatible with expense rules", HttpStatus.BAD_REQUEST);
        }
        BigDecimal normalizedLimit = requirePositiveAmount(limit);
        String normalizedCurrency = resolveCurrency(currency, null, false);
        return new SpendingLimitParams(categoryId, normalizedLimit, normalizedCurrency);
    }

    private String resolveCurrency(String currency, String fallbackCurrency, boolean allowFallback) {
        String normalized = currency == null ? "" : currencyService.normalize(currency);
        if (normalized.isBlank()) {
            if (allowFallback && fallbackCurrency != null) {
                normalized = currencyService.normalize(fallbackCurrency);
            }
        }
        if (normalized.isBlank()) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Currency is required", HttpStatus.BAD_REQUEST);
        }
        if (!currencyService.isSupported(normalized)) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported currency", HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private RuleType requireType(RuleType type) {
        if (type == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Rule type is required", HttpStatus.BAD_REQUEST);
        }
        return type;
    }

    private BigDecimal requirePositiveAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Limit is required", HttpStatus.BAD_REQUEST);
        }
        if (amount.signum() <= 0) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Limit must be greater than zero", HttpStatus.BAD_REQUEST);
        }
        return amount;
    }

    private RuleStatus resolveStatus(Boolean active) {
        if (active == null || active) {
            return RuleStatus.ACTIVE;
        }
        return RuleStatus.DISABLED;
    }

    private RuleDto toDto(Rule rule) {
        SpendingLimitParams params = ruleParamsCodec.decode(rule.getParamsJson());
        return new RuleDto(
                rule.getId(),
                rule.getType(),
                rule.getStatus(),
                params.categoryId(),
                params.limit(),
                params.currency(),
                rule.getCreatedAt(),
                rule.getUpdatedAt(),
                rule.getLastTriggeredAt()
        );
    }

    private ApiException unauthorized() {
        return new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User is not authenticated", HttpStatus.UNAUTHORIZED);
    }
}
