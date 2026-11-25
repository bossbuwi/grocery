package com.paradoxdevs.grocery.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long itemCount;
    private long storeCount;
}
