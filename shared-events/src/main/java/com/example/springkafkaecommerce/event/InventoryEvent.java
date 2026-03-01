package com.example.springkafkaecommerce.event;

import java.util.List;

public record InventoryEvent(
        String orderUuid,
        List<ProductReservationItem> reserveProducts
) {}
