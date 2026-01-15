package com.myname.finguard.rules.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.categories.model.Category;
import com.myname.finguard.categories.repository.CategoryRepository;
import com.myname.finguard.notifications.repository.NotificationRepository;
import com.myname.finguard.rules.model.Rule;
import com.myname.finguard.rules.model.RuleStatus;
import com.myname.finguard.rules.model.RuleType;
import com.myname.finguard.rules.repository.RuleRepository;
import com.myname.finguard.transactions.model.TransactionType;
import com.myname.finguard.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RuleEvaluationServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2024-01-15T12:00:00Z");

    @Mock
    private RuleRepository ruleRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private com.myname.finguard.auth.repository.UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private com.myname.finguard.common.service.MoneyConversionService moneyConversionService;
    @Mock
    private RuleParamsCodec ruleParamsCodec;

    private RuleEvaluationService ruleEvaluationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ruleEvaluationService = new RuleEvaluationService(
                ruleRepository,
                notificationRepository,
                transactionRepository,
                userRepository,
                categoryRepository,
                moneyConversionService,
                ruleParamsCodec
        );
    }

    @Test
    void createsNotificationWhenLimitReached() {
        Rule rule = new Rule();
        rule.setId(1L);
        rule.setType(RuleType.SPENDING_LIMIT_CATEGORY_MONTHLY);
        rule.setStatus(RuleStatus.ACTIVE);
        rule.setParamsJson("{}");

        User user = new User();
        user.setId(10L);
        user.setBaseCurrency("USD");
        rule.setUser(user);

        SpendingLimitParams params = new SpendingLimitParams(100L, new BigDecimal("100.00"), "USD");
        when(ruleParamsCodec.decode("{}")).thenReturn(params);

        TransactionRepository.CategoryCurrencyTotal total = new TransactionRepository.CategoryCurrencyTotal() {
            @Override
            public String getCurrency() {
                return "USD";
            }

            @Override
            public BigDecimal getTotal() {
                return new BigDecimal("120.00");
            }
        };
        when(transactionRepository.sumByCategoryAndTypeAndCurrency(
                eq(10L), eq(100L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(List.of(total));

        when(moneyConversionService.buildContext(eq("USD"), anyList()))
                .thenReturn(new com.myname.finguard.common.service.MoneyConversionService.ConversionContext(
                        "USD", 2, Map.of(), Map.of()));
        when(moneyConversionService.convertToBase(any(BigDecimal.class), any(String.class), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category category = new Category();
        category.setId(100L);
        category.setName("Food");
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));

        when(notificationRepository.existsByRuleIdAndPeriodStart(eq(1L), any())).thenReturn(false);

        ruleEvaluationService.evaluateRule(rule, FIXED_NOW);

        verify(notificationRepository).save(any());
        verify(ruleRepository).save(rule);
        assertThat(rule.getLastTriggeredAt()).isEqualTo(FIXED_NOW);
    }
}
