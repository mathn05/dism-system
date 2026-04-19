package com.csdlpt.web.repository;

import com.csdlpt.web.entity.OrderEntity;
import com.csdlpt.web.entity.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    @Override
    @EntityGraph(attributePaths = {"user", "customer", "fromStation", "toStation"})
    List<OrderEntity> findAll();

    @EntityGraph(attributePaths = {"user", "customer", "fromStation", "toStation"})
    List<OrderEntity> findByToStation_IdOrderByCreatedAtDesc(String stationId);

    long countByToStation_Id(String stationId);

    long countByToStation_IdAndType(String stationId, OrderType type);

    @Query("""
        select o.customer.id, o.customer.name, count(o)
        from OrderEntity o
        where o.toStation.id = :stationId
          and o.type = com.csdlpt.web.entity.OrderType.SALE
          and o.customer is not null
        group by o.customer.id, o.customer.name
        order by count(o) desc, o.customer.id asc
        """)
    List<Object[]> findTopCustomersByStation(@Param("stationId") String stationId);
}

