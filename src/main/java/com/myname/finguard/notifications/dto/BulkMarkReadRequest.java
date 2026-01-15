package com.myname.finguard.notifications.dto;

import java.util.List;

public record BulkMarkReadRequest(List<Long> ids) {
}
