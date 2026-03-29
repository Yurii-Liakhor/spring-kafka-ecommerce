package com.example.springkafkaecommerce.repository;

import com.example.springkafkaecommerce.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();

    boolean existsByAggregateIdAndTopic(String aggregateId, String topic);

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEvent e SET e.published = true WHERE e.id = :id")
    void markPublished(@Param("id") Long id);
}