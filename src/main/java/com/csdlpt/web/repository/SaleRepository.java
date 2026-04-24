package com.csdlpt.web.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.csdlpt.web.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, String> {

    @EntityGraph(attributePaths = {"user", "customer", "station"})
    List<Sale> findByStation_IdOrderByCreatedAtDescIdDesc(String stationId);

    @Query("""
        select c.name, count(s)
        from Sale s
        join s.customer c
        where s.station.id = :stationId
        group by c.id, c.name
        order by count(s) desc, c.name asc
        """)
    List<Object[]> findTopCustomersByStationId(@Param("stationId") String stationId);
}
