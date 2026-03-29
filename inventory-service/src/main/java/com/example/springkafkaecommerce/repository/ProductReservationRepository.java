package com.example.springkafkaecommerce.repository;

import com.example.springkafkaecommerce.entity.ProductReservation;
import com.example.springkafkaecommerce.model.ReservationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReservationRepository extends JpaRepository<ProductReservation, Long> {

    boolean existsByOrderUuid(String orderUuid);

    boolean existsByOrderUuidAndState(String orderUuid, ReservationState state);

    List<ProductReservation> findAllByOrderUuid(String orderUuid);

    @Query("""
            select coalesce(sum(pr.reservedQuantity), 0)
            from ProductReservation pr
            where pr.product.id = :productId
              and pr.state = :state
           """)
    int sumReservedQuantityByProductIdAndState(Long productId, ReservationState state);
}
