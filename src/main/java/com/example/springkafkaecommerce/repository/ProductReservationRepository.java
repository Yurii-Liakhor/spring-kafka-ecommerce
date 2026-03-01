package com.example.springkafkaecommerce.repository;

import com.example.springkafkaecommerce.entity.ProductReservation;
import com.example.springkafkaecommerce.model.ReservationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReservationRepository extends JpaRepository<ProductReservation, Long> {

    @Query("""
            select coalesce(sum(pr.reservedQuantity), 0)
            from ProductReservation pr
            where pr.product.id = :productId
              and pr.state = :state
           """)
    int sumReservedQuantityByProductIdAndState(Long productId, ReservationState state);
}
