package com.example.springkafkaecommerce.service.impl;

import com.example.springkafkaecommerce.entity.Product;
import com.example.springkafkaecommerce.entity.ProductReservation;
import com.example.springkafkaecommerce.event.ProductReservationItem;
import com.example.springkafkaecommerce.exception.OutOfStockException;
import com.example.springkafkaecommerce.model.ReservationState;
import com.example.springkafkaecommerce.repository.InventoryRepository;
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
    public void reserveProducts(List<ProductReservationItem> products) {
        log.debug("Reserving products: {}", products);
        for (ProductReservationItem item : products) {
            int updated = inventoryRepository.tryReserve(item.productId(), item.quantity());
            if (updated < 1) {
                log.warn("Not enough quantity for reservation product: {}", item.productId());
                throw new OutOfStockException(item.productId());
            }

            Product product = productRepository.getReferenceById(item.productId());
            productReservationRepository.save(ProductReservation.builder()
                    .product(product)
                    .reservedQuantity(item.quantity())
                    .state(ReservationState.ACTIVE)
                    .createdAt(Instant.now())
                    .build());
        }
    }
}