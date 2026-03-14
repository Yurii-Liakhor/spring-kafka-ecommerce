package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.event.ProductReservationItem;

import java.util.List;

public interface InventoryService {

    void reserveProducts(String orderUuid, List<ProductReservationItem> productReservationItems);
}