package com.example.springkafkaecommerce.service.impl;

import com.example.springkafkaecommerce.entity.Product;
import com.example.springkafkaecommerce.entity.ProductReservation;
import com.example.springkafkaecommerce.event.ProductReservationItem;
import com.example.springkafkaecommerce.exception.OutOfStockException;
import com.example.springkafkaecommerce.model.ReservationState;
import com.example.springkafkaecommerce.repository.InventoryRepository;
import com.example.springkafkaecommerce.repository.ProductRepository;
import com.example.springkafkaecommerce.repository.ProductReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    private static final String ORDER_UUID = "cb59a1b2-415a-4707-8d77-45b86ab5262f";
    private static final long PRODUCT_ID_1 = 1L;
    private static final long PRODUCT_ID_2 = 2L;
    private static final int QUANTITY_1 = 3;
    private static final int QUANTITY_2 = 5;
    private static final String PRODUCT_NAME_1 = "Product 1";
    private static final String PRODUCT_NAME_2 = "Product 2";

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ProductReservationRepository productReservationRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryServiceImpl sut;

    @Captor
    private ArgumentCaptor<ProductReservation> reservationCaptor;

    @Test
    void reserveProducts_whenAlreadyReserved_shouldSkip() {
        ProductReservationItem item = buildReservationItem(PRODUCT_ID_1, QUANTITY_1);

        when(productReservationRepository.existsByOrderUuid(ORDER_UUID)).thenReturn(true);

        sut.reserveProducts(ORDER_UUID, List.of(item));

        verify(inventoryRepository, never()).tryReserve(any(), anyInt());
        verify(productReservationRepository, never()).save(any());
    }

    @Test
    void reserveProducts_whenStockSufficient_shouldSaveReservations() {
        Product product1 = buildProduct(PRODUCT_ID_1, PRODUCT_NAME_1);
        Product product2 = buildProduct(PRODUCT_ID_2, PRODUCT_NAME_2);
        ProductReservationItem reservationItem1 = buildReservationItem(PRODUCT_ID_1, QUANTITY_1);
        ProductReservationItem reservationItem2 = buildReservationItem(PRODUCT_ID_2, QUANTITY_2);

        when(productReservationRepository.existsByOrderUuid(ORDER_UUID)).thenReturn(false);
        when(inventoryRepository.tryReserve(PRODUCT_ID_1, QUANTITY_1)).thenReturn(1);
        when(inventoryRepository.tryReserve(PRODUCT_ID_2, QUANTITY_2)).thenReturn(1);
        when(productRepository.getReferenceById(PRODUCT_ID_1)).thenReturn(product1);
        when(productRepository.getReferenceById(PRODUCT_ID_2)).thenReturn(product2);

        sut.reserveProducts(ORDER_UUID, List.of(reservationItem1, reservationItem2));

        verify(productReservationRepository, times(2)).save(reservationCaptor.capture());

        List<ProductReservation> savedReservations = reservationCaptor.getAllValues();

        assertThat(savedReservations).hasSize(2);

        assertThat(savedReservations.getFirst().getOrderUuid()).isEqualTo(ORDER_UUID);
        assertThat(savedReservations.getFirst().getProduct()).isEqualTo(product1);
        assertThat(savedReservations.getFirst().getReservedQuantity()).isEqualTo(QUANTITY_1);
        assertThat(savedReservations.getFirst().getState()).isEqualTo(ReservationState.ACTIVE);
        assertThat(savedReservations.getFirst().getCreatedAt()).isNotNull();

        assertThat(savedReservations.get(1).getOrderUuid()).isEqualTo(ORDER_UUID);
        assertThat(savedReservations.get(1).getProduct()).isEqualTo(product2);
        assertThat(savedReservations.get(1).getReservedQuantity()).isEqualTo(QUANTITY_2);
        assertThat(savedReservations.get(1).getState()).isEqualTo(ReservationState.ACTIVE);
        assertThat(savedReservations.get(1).getCreatedAt()).isNotNull();
    }

    @Test
    void reserveProducts_whenFirstItemOutOfStock_shouldThrowAndNotSave() {
        ProductReservationItem reservationItem1 = buildReservationItem(PRODUCT_ID_1, QUANTITY_1);
        ProductReservationItem reservationItem2 = buildReservationItem(PRODUCT_ID_2, QUANTITY_2);

        when(productReservationRepository.existsByOrderUuid(ORDER_UUID)).thenReturn(false);
        when(inventoryRepository.tryReserve(PRODUCT_ID_1, QUANTITY_1)).thenReturn(0);

        assertThatThrownBy(() -> sut.reserveProducts(ORDER_UUID, List.of(reservationItem1, reservationItem2)))
                .isInstanceOf(OutOfStockException.class)
                .hasMessage("Not enough quantity for reservation product: " + PRODUCT_ID_1);

        verify(productReservationRepository, never()).save(any());
    }

    @Test
    void reserveProducts_whenSecondItemOutOfStock_shouldThrowAfterFirstReserved() {
        Product product1 = buildProduct(PRODUCT_ID_1, PRODUCT_NAME_1);
        ProductReservationItem reservationItem1 = buildReservationItem(PRODUCT_ID_1, QUANTITY_1);
        ProductReservationItem reservationItem2 = buildReservationItem(PRODUCT_ID_2, QUANTITY_2);

        when(productReservationRepository.existsByOrderUuid(ORDER_UUID)).thenReturn(false);
        when(inventoryRepository.tryReserve(PRODUCT_ID_1, QUANTITY_1)).thenReturn(1);
        when(inventoryRepository.tryReserve(PRODUCT_ID_2, QUANTITY_2)).thenReturn(0);
        when(productRepository.getReferenceById(PRODUCT_ID_1)).thenReturn(product1);

        assertThatThrownBy(() -> sut.reserveProducts(ORDER_UUID, List.of(reservationItem1, reservationItem2)))
                .isInstanceOf(OutOfStockException.class)
                .hasMessage("Not enough quantity for reservation product: " + PRODUCT_ID_2);

        verify(productReservationRepository, times(1)).save(any());
    }

    @Test
    void reserveProducts_withEmptyList_shouldSaveNothing() {
        when(productReservationRepository.existsByOrderUuid(ORDER_UUID)).thenReturn(false);

        sut.reserveProducts(ORDER_UUID, List.of());

        verify(inventoryRepository, never()).tryReserve(any(), anyInt());
        verify(productReservationRepository, never()).save(any());
    }

    @Test
    void releaseProducts_whenNoActiveReservation_shouldSkip() {
        ProductReservationItem reservationItem = buildReservationItem(PRODUCT_ID_1, QUANTITY_1);

        when(productReservationRepository.existsByOrderUuidAndState(ORDER_UUID, ReservationState.ACTIVE))
                .thenReturn(false);

        sut.releaseProducts(ORDER_UUID, List.of(reservationItem));

        verify(inventoryRepository, never()).releaseReserve(any(), anyInt());
        verify(productReservationRepository, never()).saveAll(any());
    }

    @Test
    void releaseProducts_whenActiveReservationExists_shouldReleaseAndMarkReleased() {
        Product product1 = buildProduct(PRODUCT_ID_1, PRODUCT_NAME_1);
        Product product2 = buildProduct(PRODUCT_ID_2, PRODUCT_NAME_2);
        ProductReservationItem reservationItem1 = buildReservationItem(PRODUCT_ID_1, QUANTITY_1);
        ProductReservationItem reservationItem2 = buildReservationItem(PRODUCT_ID_2, QUANTITY_2);
        ProductReservation reservation1 = ProductReservation.builder()
                .orderUuid(ORDER_UUID).product(product1).reservedQuantity(QUANTITY_1)
                .state(ReservationState.ACTIVE).build();
        ProductReservation reservation2 = ProductReservation.builder()
                .orderUuid(ORDER_UUID).product(product2).reservedQuantity(QUANTITY_2)
                .state(ReservationState.ACTIVE).build();

        when(productReservationRepository.existsByOrderUuidAndState(ORDER_UUID, ReservationState.ACTIVE))
                .thenReturn(true);
        when(inventoryRepository.releaseReserve(PRODUCT_ID_1, QUANTITY_1)).thenReturn(1);
        when(inventoryRepository.releaseReserve(PRODUCT_ID_2, QUANTITY_2)).thenReturn(1);
        when(productReservationRepository.findAllByOrderUuid(ORDER_UUID))
                .thenReturn(List.of(reservation1, reservation2));

        sut.releaseProducts(ORDER_UUID, List.of(reservationItem1, reservationItem2));

        verify(inventoryRepository).releaseReserve(PRODUCT_ID_1, QUANTITY_1);
        verify(inventoryRepository).releaseReserve(PRODUCT_ID_2, QUANTITY_2);

        assertThat(reservation1.getState()).isEqualTo(ReservationState.RELEASED);
        assertThat(reservation2.getState()).isEqualTo(ReservationState.RELEASED);

        verify(productReservationRepository).saveAll(List.of(reservation1, reservation2));
    }

    @Test
    void releaseProducts_whenReleaseReserveFails_shouldThrowRuntimeException() {
        ProductReservationItem reservationItem = buildReservationItem(PRODUCT_ID_1, QUANTITY_1);

        when(productReservationRepository.existsByOrderUuidAndState(ORDER_UUID, ReservationState.ACTIVE))
                .thenReturn(true);
        when(inventoryRepository.releaseReserve(PRODUCT_ID_1, QUANTITY_1)).thenReturn(0);

        assertThatThrownBy(() -> sut.releaseProducts(ORDER_UUID, List.of(reservationItem)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to release product reservation");

        verify(productReservationRepository, never()).findAllByOrderUuid(any());
        verify(productReservationRepository, never()).saveAll(any());
    }

    private static Product buildProduct(long id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private static ProductReservationItem buildReservationItem(long productId, int qty) {
        return new ProductReservationItem(productId, qty);
    }
}