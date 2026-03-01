package com.example.springkafkaecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOrderResponse {
    private List<InventoryProductResponse> inventoryProductResponses;
}
