package com.csdlpt.web.repository;

import com.csdlpt.web.entity.OrderEntity;
import com.csdlpt.web.entity.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    @Override
    @EntityGraph(attributePaths = {"user", "customer", "fromStation", "toStation"})
    List<OrderEntity> findAll();

    @EntityGraph(attributePaths = {"user", "customer", "fromStation", "toStation"})
    List<OrderEntity> findByToStation_IdOrderByCreatedAtDesc(String stationId);

    long countByToStation_Id(String stationId);

    long countByToStation_IdAndType(String stationId, OrderType type);
}

