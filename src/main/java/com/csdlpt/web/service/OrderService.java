package com.csdlpt.web.service;

import com.csdlpt.web.entity.OrderEntity;
import com.csdlpt.web.entity.OrderType;
import com.csdlpt.web.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    public record TopCustomerSummary(String customerId, String customerName, long orderCount) {}

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {

        this.orderRepository = orderRepository;
    }

    public List<OrderEntity> findAll() {

        return orderRepository.findAll();
    }

    public List<OrderEntity> findByStation(String stationId) {
        return orderRepository.findByToStation_IdOrderByCreatedAtDesc(stationId);
    }

    public long countByStation(String stationId) {

        return orderRepository.countByToStation_Id(stationId);
    }

    public long countByStationAndType(String stationId, OrderType type) {
        return orderRepository.countByToStation_IdAndType(stationId, type);
    }

    public OrderEntity save(OrderEntity order) {

        return orderRepository.save(order);
    }

    public OrderEntity findById(String id) {

        return orderRepository.findById(id).orElse(null);
    }

    public TopCustomerSummary findTopCustomerByStation(String stationId) {
        return orderRepository.findTopCustomersByStation(stationId).stream()
            .findFirst()
            .map(result -> new TopCustomerSummary(
                String.valueOf(result[0]),
                String.valueOf(result[1]),
                ((Number) result[2]).longValue()
            ))
            .orElse(null);
    }
}
