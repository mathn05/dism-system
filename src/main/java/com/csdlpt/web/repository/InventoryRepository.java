package com.csdlpt.web.repository;

import java.time.LocalDate;
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

    @Query("""
                select coalesce(sum(i.quantity),0)
                from Inventory i
            """)
    Integer sumAllQuantity();

    @Query("""
                select count(i)
                from Inventory i
                where i.quantity <= :value
            """)
    Long countLowStock(@Param("value") int value);

    @Query("""
            select sum(i.quantity)
            from Inventory i
            group by i.station.name
            """)
    List<Integer> sumByStation();

    @Query("""
            SELECT SUM(i.quantity)
            FROM Inventory i
            """)
    Long getTotalInventory();

    @Query("""
            SELECT p.name, SUM(sd.quantity)
            FROM SaleDetail sd
            JOIN sd.product p
            GROUP BY p.name
            ORDER BY SUM(sd.quantity) DESC
            """)
    List<Object[]> topProducts();


    @Query("""
            SELECT s.name, SUM(i.quantity)
            FROM Inventory i
            JOIN i.station s
            GROUP BY s.name
            """)
    List<Object[]> inventoryByStation();

    @Query("""
                SELECT SUM(i.quantity)
                FROM Inventory i
                WHERE i.station.id = :stationId
            """)
    Long getTotalInventoryByStation(@Param("stationId") String stationId);

    @Query("""
                SELECT i.station.name, SUM(i.quantity)
                FROM Inventory i
                WHERE i.station.id = :stationId
                GROUP BY i.station.name
            """)
    List<Object[]> inventoryByStation(@Param("stationId") String stationId);

}


