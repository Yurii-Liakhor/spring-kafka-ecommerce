package com.example.springkafkaecommerce.service.impl;

import com.example.springkafkaecommerce.entity.Order;
import com.example.springkafkaecommerce.entity.Product;
import com.example.springkafkaecommerce.entity.ProductReservation;
import com.example.springkafkaecommerce.event.ProductReservationItem;
import com.example.springkafkaecommerce.model.ReservationState;
import com.example.springkafkaecommerce.repository.InventoryRepository;
import com.example.springkafkaecommerce.repository.OrderRepository;
import com.example.springkafkaecommerce.repository.ProductRepository;
import com.example.springkafkaecommerce.repository.ProductReservationRepository;
import com.example.springkafkaecommerce.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final ProductReservationRepository productReservationRepository;
    private final ProductRepository productRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                ProductReservationRepository productReservationRepository,
                                ProductRepository productRepository) {
        this.productReservationRepository = productReservationRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    @Override
    public void reserveProducts(String orderUuid, List<ProductReservationItem> products) {
        log.debug("Reserving products: {}", products);
        for (ProductReservationItem product : products) {
            reserveProduct(orderUuid, product);
        }
    }

    public void reserveProduct(String orderUuid, ProductReservationItem item) {
        log.debug("Reserving product: {}", item.productId());

        Long productId = item.productId();
        int requested = item.quantity();

        int updated = inventoryRepository.tryReserve(productId, requested);
        if (updated < 1) {
            log.warn("Not enough quantity for reservation product: {}", productId);
            throw new IllegalStateException("Not enough quantity for reservation product: " + productId);
        }

        Product product = productRepository.getReferenceById(productId);

        productReservationRepository.save(ProductReservation.builder()
                .product(product)
                .reservedQuantity(requested)
                .state(ReservationState.ACTIVE)
                .createdAt(Instant.now())
                .build());
    }

}
