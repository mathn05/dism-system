package com.csdlpt.web.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.csdlpt.web.entity.Inventory;
import com.csdlpt.web.entity.InventoryId;

@SuppressWarnings("unused")
public interface InventoryRepository extends JpaRepository<Inventory, InventoryId> {

    @EntityGraph(attributePaths = {"product", "product.category", "station"})
    List<Inventory> findByStation_IdOrderByProduct_IdAsc(String stationId);

    @EntityGraph(attributePaths = {"product", "product.category", "station"})
    Optional<Inventory> findByStation_IdAndProduct_Id(String stationId, String productId);

    long countByStation_Id(String stationId);

    long countByStation_IdAndQuantityLessThanEqual(String stationId, int quantity);

    @Query("select coalesce(sum(i.quantity), 0) from Inventory i where i.station.id = :stationId")
    long sumQuantityByStationId(@Param("stationId") String stationId);
}


