package com.myname.finguard.rules.service;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.categories.model.Category;
import com.myname.finguard.categories.repository.CategoryRepository;
import com.myname.finguard.common.service.MoneyConversionService;
import com.myname.finguard.notifications.model.Notification;
import com.myname.finguard.notifications.repository.NotificationRepository;
import com.myname.finguard.rules.model.Rule;
import com.myname.finguard.rules.model.RuleStatus;
import com.myname.finguard.rules.model.RuleType;
import com.myname.finguard.rules.repository.RuleRepository;
import com.myname.finguard.transactions.model.TransactionType;
import com.myname.finguard.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class RuleEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationService.class);
    private static final int BATCH_SIZE = 200;

    private final RuleRepository ruleRepository;
    private final NotificationRepository notificationRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MoneyConversionService moneyConversionService;
    private final RuleParamsCodec ruleParamsCodec;

    public RuleEvaluationService(
            RuleRepository ruleRepository,
            NotificationRepository notificationRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            MoneyConversionService moneyConversionService,
            RuleParamsCodec ruleParamsCodec
    ) {
        this.ruleRepository = ruleRepository;
        this.notificationRepository = notificationRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.moneyConversionService = moneyConversionService;
        this.ruleParamsCodec = ruleParamsCodec;
    }

    public void evaluateActiveRules() {
        evaluateActiveRules(Instant.now());
    }

    public void evaluateActiveRules(Instant now) {
        int page = 0;
        Page<Rule> batch;
        do {
            batch = ruleRepository.findByStatus(RuleStatus.ACTIVE, PageRequest.of(page, BATCH_SIZE, Sort.by("id")));
            for (Rule rule : batch) {
                try {
                    evaluateRule(rule, now);
                } catch (Exception ex) {
                    log.warn("Failed to evaluate rule {}", rule == null ? null : rule.getId(), ex);
                }
            }
            page++;
        } while (batch.hasNext());
    }

    void evaluateRule(Rule rule, Instant now) {
        if (rule == null || rule.getStatus() != RuleStatus.ACTIVE) {
            return;
        }
        if (rule.getId() == null) {
            return;
        }
        if (rule.getType() != RuleType.SPENDING_LIMIT_CATEGORY_MONTHLY) {
            return;
        }
        SpendingLimitParams params = ruleParamsCodec.decode(rule.getParamsJson());
        if (params.categoryId() == null || params.limit() == null || params.limit().signum() <= 0) {
            return;
        }
        Long userId = rule.getUser() == null ? null : rule.getUser().getId();
        if (userId == null) {
            return;
        }
        User user = resolveUser(rule.getUser(), userId);
        if (user == null || user.getBaseCurrency() == null || user.getBaseCurrency().isBlank()) {
            return;
        }

        MonthWindow window = resolveMonthWindow(now);
        if (rule.getId() != null && notificationRepository.existsByRuleIdAndPeriodStart(rule.getId(), window.periodStart())) {
            return;
        }

        List<TransactionRepository.CategoryCurrencyTotal> totals = transactionRepository.sumByCategoryAndTypeAndCurrency(
                userId,
                params.categoryId(),
                TransactionType.EXPENSE,
                window.from(),
                window.to()
        );

        String baseCurrency = user.getBaseCurrency();
        List<String> currencies = new ArrayList<>();
        if (totals != null) {
            for (var row : totals) {
                if (row != null && row.getCurrency() != null) {
                    currencies.add(row.getCurrency());
                }
            }
        }
        if (params.currency() != null && !params.currency().isBlank()) {
            currencies.add(params.currency());
        }

        MoneyConversionService.ConversionContext ctx = moneyConversionService.buildContext(baseCurrency, currencies);
        BigDecimal spent = BigDecimal.ZERO;
        if (totals != null) {
            for (var row : totals) {
                if (row == null || row.getCurrency() == null || row.getTotal() == null) {
                    continue;
                }
                spent = spent.add(moneyConversionService.convertToBase(row.getTotal(), row.getCurrency(), ctx));
            }
        }
        spent = spent.setScale(ctx.scale(), RoundingMode.HALF_UP);

        BigDecimal limit = params.limit();
        if (params.currency() != null
                && !params.currency().isBlank()
                && !params.currency().equalsIgnoreCase(baseCurrency)) {
            limit = moneyConversionService.convertToBase(params.limit(), params.currency(), ctx);
        }
        limit = limit.setScale(ctx.scale(), RoundingMode.HALF_UP);

        if (spent.compareTo(limit) < 0) {
            return;
        }

        String categoryName = resolveCategoryName(userId, params.categoryId());
        String message = buildMessage(categoryName, spent, limit, baseCurrency);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setRule(rule);
        notification.setMessage(message);
        notification.setPeriodStart(window.periodStart());
        notification.setCreatedAt(now);

        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException ex) {
            return;
        }

        rule.setLastTriggeredAt(now);
        ruleRepository.save(rule);
    }

    private User resolveUser(User current, Long userId) {
        if (current != null && current.getBaseCurrency() != null) {
            return current;
        }
        return userRepository.findById(userId).orElse(null);
    }

    private String resolveCategoryName(Long userId, Long categoryId) {
        if (categoryId == null) {
            return "Category";
        }
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return "Category";
        }
        if (category.getUser() != null && (category.getUser().getId() == null || !category.getUser().getId().equals(userId))) {
            return "Category";
        }
        String name = category.getName();
        return name == null || name.isBlank() ? "Category" : name;
    }

    private String buildMessage(String categoryName, BigDecimal spent, BigDecimal limit, String baseCurrency) {
        String safeCategory = categoryName == null || categoryName.isBlank() ? "Category" : categoryName;
        String currency = baseCurrency == null ? "" : baseCurrency.trim();
        return "Monthly spending limit reached for " + safeCategory + ": "
                + toPlain(spent) + " " + currency + " of " + toPlain(limit) + " " + currency + ".";
    }

    private String toPlain(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private MonthWindow resolveMonthWindow(Instant now) {
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        LocalDate start = today.withDayOfMonth(1);
        LocalDate next = start.plusMonths(1);
        Instant from = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = next.atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1);
        return new MonthWindow(start, from, to);
    }

    private record MonthWindow(LocalDate periodStart, Instant from, Instant to) {
    }
}
