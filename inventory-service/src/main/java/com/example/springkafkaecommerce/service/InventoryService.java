package com.example.springkafkaecommerce.service;

import com.example.springkafkaecommerce.event.ProductReservationItem;

import java.util.List;

public interface InventoryService {

    void reserveProducts(List<ProductReservationItem> productReservationItems);
}