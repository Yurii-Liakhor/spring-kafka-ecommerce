package com.example.springkafkaecommerce.repository;

import com.example.springkafkaecommerce.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    int getQuantityByProductId(Long productId);

    @Query("""
        select i.onHandQuantity - i.reservedQuantity
          from Inventory i
         where i.product.id = :productId
    """)
    int getAvailableQuantityByProductId(Long productId);

    @Modifying
    @Query("""
        update Inventory i
           set i.reservedQuantity = i.reservedQuantity + :quantity
         where i.product.id = :productId
           and (i.onHandQuantity - i.reservedQuantity) >= :quantity
    """)
    int tryReserve(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying
    @Query("""
        update Inventory i
           set i.reservedQuantity = i.reservedQuantity - :quantity
         where i.product.id = :productId
           and i.reservedQuantity >= :quantity
    """)
    int releaseReserve(@Param("productId") Long productId, @Param("quantity") int quantity);
}
