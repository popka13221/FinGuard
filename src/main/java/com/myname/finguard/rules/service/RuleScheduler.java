package com.myname.finguard.rules.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RuleScheduler {

    private final RuleEvaluationService ruleEvaluationService;

    public RuleScheduler(RuleEvaluationService ruleEvaluationService) {
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Scheduled(cron = "${app.rules.scheduler-cron:0 0 * * * *}")
    public void runRules() {
        ruleEvaluationService.evaluateActiveRules();
    }
}
