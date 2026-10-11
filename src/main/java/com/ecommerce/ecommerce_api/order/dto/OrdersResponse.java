package com.ecommerce.ecommerce_api.order.dto;

import java.util.List;

public record OrdersResponse(List<OrderResponse> orders) {
}