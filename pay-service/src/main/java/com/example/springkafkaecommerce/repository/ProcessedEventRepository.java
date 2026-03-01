package com.example.springkafkaecommerce.repository;

import com.example.springkafkaecommerce.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    boolean existsByEventIdAndTopic(String eventId, String topic);

    void deleteByEventIdAndTopic(String eventId, String topic);
}