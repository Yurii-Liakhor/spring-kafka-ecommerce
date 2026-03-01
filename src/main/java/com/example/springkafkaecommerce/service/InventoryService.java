package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.event.ProductReservationItem;

import java.util.List;

public interface InventoryService {
//    InventoryOrderResponse checkOrderAvailability(List<ProductReservationItem> productReservationItems);
//    InventoryProductResponse checkProductAvailability(ProductReservationItem productReservationItem);
    void reserveProducts(String orderUuid, List<ProductReservationItem> productReservationItems);
}