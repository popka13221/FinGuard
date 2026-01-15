package com.myname.finguard.rules.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RuleParamsCodec {

    private final ObjectMapper objectMapper;

    public RuleParamsCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(SpendingLimitParams params) {
        if (params == null) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Rule params are required", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Failed to serialize rule params", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public SpendingLimitParams decode(String json) {
        if (json == null || json.isBlank()) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Rule params are missing", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            return objectMapper.readValue(json, SpendingLimitParams.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorCodes.INTERNAL_ERROR, "Failed to parse rule params", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
