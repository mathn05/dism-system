package com.csdlpt.web.repository;

import java.time.LocalDate;
import java.util.List;

import com.csdlpt.web.dto.TopStationDto;
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

    @Query("""
                select count(s)
                from Sale s
                where cast(s.createdAt as date)=:date
            """)
    Long countByDate(LocalDate date);

    @Query(value = """
                select top 5
                    st.name as stationName,
                    count(*) as totalOrders,
            
                    count(*) * 100 /
                    (
                        select max(x.total)
                        from (
                            select count(*) total
                            from Sale
                            group by station_id
                        ) x
                    ) as percentage
            
                from Sale sa
                join Station st on sa.station_id = st.id
                group by st.name
                order by totalOrders desc
            """, nativeQuery = true)
    List<TopStationDto> getTopStations();

    @Query("""
                SELECT i.station.name, SUM(sd.quantity)
                FROM Sale i
                JOIN SaleDetail sd ON sd.sale.id = i.id
                GROUP BY i.station.name
            """)
    List<Object[]> saleByStation();

    @Query("""
                SELECT SUM(d.quantity)
                FROM Sale s
                JOIN SaleDetail d ON d.sale.id = s.id
                GROUP BY CAST(s.createdAt AS date)
                ORDER BY CAST(s.createdAt AS date)
            """)
    List<Long> saleData();

    @Query("""
                SELECT CAST(s.createdAt AS date)
                FROM Sale s
                GROUP BY CAST(s.createdAt AS date)
                ORDER BY CAST(s.createdAt AS date)
            """)
    List<Object> revenueLabels();

    @Query("""
                SELECT SUM(d.quantity * d.salePrice)
                FROM Sale s
                JOIN SaleDetail d ON d.sale.id = s.id
                GROUP BY CAST(s.createdAt AS date)
                ORDER BY CAST(s.createdAt AS date)
            """)
    List<Long> revenueData();

    @Query("""
                SELECT COALESCE(SUM(d.quantity * d.salePrice), 0)
                FROM Sale s
                JOIN SaleDetail d ON d.sale.id = s.id
            """)
    Long totalRevenue();

    @Query("""
                SELECT COALESCE(COUNT(s.id), 0)
                FROM Sale s
            """)
    Long totalOrders();

    @Query("""
                SELECT s.station.name, SUM(d.quantity * d.salePrice)
                FROM Sale s
                JOIN SaleDetail d ON d.sale.id = s.id
                WHERE s.station.id = :stationId
                GROUP BY s.station.name
            """)
    List<Object[]> saleByStation(@Param("stationId") String stationId);

    @Query("""
                SELECT SUM(d.quantity)
                FROM Sale s
                JOIN SaleDetail d ON d.sale.id = s.id
                WHERE s.station.id = :stationId
                GROUP BY CAST(s.createdAt AS date)
                ORDER BY CAST(s.createdAt AS date)
            """)
    List<Long> saleData(@Param("stationId") String stationId);

    @Query("""
                SELECT COALESCE(SUM(d.quantity * d.salePrice), 0)
                FROM Sale s
                JOIN SaleDetail d ON d.sale.id = s.id
                WHERE s.station.id = :stationId
            """)
    Long totalRevenueByStation(@Param("stationId") String stationId);

    @Query("""
                SELECT CAST(s.createdAt AS date)
                FROM Sale s
                WHERE s.station.id = :stationId
                GROUP BY CAST(s.createdAt AS date)
                ORDER BY CAST(s.createdAt AS date)
            """)
    List<Object[]> revenueLabels(@Param("stationId") String stationId);

    @Query("""
                SELECT SUM(d.quantity * d.salePrice)
                FROM Sale s
                JOIN SaleDetail d ON d.sale.id = s.id
                WHERE s.station.id = :stationId
                GROUP BY CAST(s.createdAt AS date)
                ORDER BY CAST(s.createdAt AS date)
            """)
    List<Long> revenueData(@Param("stationId") String stationId);

    @Query("""
                SELECT COUNT(s)
                FROM Sale s
                WHERE s.station.id = :stationId
            """)
    Long totalOrdersByStation(@Param("stationId") String stationId);

    @Query(value = """
            SELECT 
                ISNULL(SUM(sd.quantity * sd.sale_price), 0)
                -
                ISNULL(
                    SUM(sd.quantity) *
                    (
                        SELECT 
                            SUM(d.quantity * d.import_price) * 1.0 / NULLIF(SUM(d.quantity), 0)
                        FROM ImportDetail d
                    ),
                0)
            FROM SaleDetail sd
            """, nativeQuery = true)
    Long profit();

    @Query(value = """
            SELECT 
                ISNULL(SUM(sd.quantity * sd.sale_price), 0)
                -
                ISNULL(
                    SUM(sd.quantity) *
                    (
                        SELECT 
                            SUM(d.quantity * d.import_price) * 1.0 / NULLIF(SUM(d.quantity), 0)
                        FROM Import i
                        JOIN ImportDetail d ON d.import_id = i.id
                        WHERE i.station_id = :stationId
                    ),
                0) AS profit
            FROM Sale s
            JOIN SaleDetail sd ON sd.sale_id = s.id
            WHERE s.station_id = :stationId
            GROUP BY s.station_id
            """, nativeQuery = true)
    Long profitByStation(@Param("stationId") String stationId);

    @Query(value = """
        SELECT COALESCE(SUM(d.quantity), 0)
        FROM [Sale] s
        JOIN [SaleDetail] d ON s.id = d.sale_id
        WHERE s.station_id = :stationId
          AND CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
        GROUP BY CAST(s.created_at AS date)
        ORDER BY CAST(s.created_at AS date)
    """, nativeQuery = true)
    List<Long> saleData(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
        SELECT COALESCE(SUM(d.quantity), 0)
        FROM [Sale] s
        JOIN [SaleDetail] d ON s.id = d.sale_id
        WHERE CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
        GROUP BY CAST(s.created_at AS date)
        ORDER BY CAST(s.created_at AS date)
    """, nativeQuery = true)
    List<Long> saleData(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
    SELECT 
        COALESCE(
            SUM(sd.quantity * sd.sale_price)
            -
            SUM(
                sd.quantity * COALESCE(imp.avg_price, 0)
            ),
        0)
    FROM [Sale] s
    JOIN [SaleDetail] sd ON s.id = sd.sale_id

    LEFT JOIN (
        SELECT 
            product_id,
            SUM(quantity * import_price) * 1.0 
            / NULLIF(SUM(quantity), 0) AS avg_price
        FROM [ImportDetail]
        GROUP BY product_id
    ) imp ON sd.product_id = imp.product_id

    WHERE s.station_id = :stationId
      AND CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
""", nativeQuery = true)
    Long profitByStation(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
    SELECT 
        COALESCE(
            SUM(sd.quantity * sd.sale_price)
            -
            SUM(
                sd.quantity * COALESCE(imp.avg_price, 0)
            ),
        0)
    FROM [Sale] s
    JOIN [SaleDetail] sd ON s.id = sd.sale_id

    LEFT JOIN (
        SELECT 
            product_id,
            SUM(quantity * import_price) * 1.0
            / NULLIF(SUM(quantity), 0) AS avg_price
        FROM [ImportDetail]
        GROUP BY product_id
    ) imp ON sd.product_id = imp.product_id

    WHERE CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
""", nativeQuery = true)
    Long profit(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
    SELECT CAST(s.created_at AS date)
    FROM [Sale] s
    WHERE s.station_id = :stationId
      AND CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
    GROUP BY CAST(s.created_at AS date)
    ORDER BY CAST(s.created_at AS date)
""", nativeQuery = true)
    List<Object[]> revenueLabels(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
    SELECT CAST(s.created_at AS date)
    FROM [Sale] s
    WHERE CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
    GROUP BY CAST(s.created_at AS date)
    ORDER BY CAST(s.created_at AS date)
""", nativeQuery = true)
    List<Object> revenueLabels(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
    SELECT COALESCE(SUM(d.quantity * d.sale_price), 0)
    FROM [Sale] s
    JOIN [SaleDetail] d ON s.id = d.sale_id
    WHERE s.station_id = :stationId
      AND CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
    GROUP BY CAST(s.created_at AS date)
    ORDER BY CAST(s.created_at AS date)
""", nativeQuery = true)
    List<Long> revenueData(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
    SELECT COALESCE(SUM(d.quantity * d.sale_price), 0)
    FROM [Sale] s
    JOIN [SaleDetail] d ON s.id = d.sale_id
    WHERE CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
    GROUP BY CAST(s.created_at AS date)
    ORDER BY CAST(s.created_at AS date)
""", nativeQuery = true)
    List<Long> revenueData(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
    SELECT COALESCE(SUM(d.quantity * d.sale_price), 0)
    FROM [Sale] s
    JOIN [SaleDetail] d ON s.id = d.sale_id
    WHERE s.station_id = :stationId
      AND CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
""", nativeQuery = true)
    Long totalRevenueByStation(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
    SELECT COALESCE(SUM(d.quantity * d.sale_price), 0)
    FROM [Sale] s
    JOIN [SaleDetail] d ON s.id = d.sale_id
    WHERE CAST(s.created_at AS date) BETWEEN :fromDate AND :toDate
""", nativeQuery = true)
    Long totalRevenue(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
    SELECT COUNT(s)
    FROM Sale s
    WHERE s.station.id = :stationId
      AND CAST(s.createdAt AS date) BETWEEN :fromDate AND :toDate
""")
    Long totalOrdersByStation(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
    @Query("""
    SELECT COUNT(s)
    FROM Sale s
    WHERE CAST(s.createdAt AS date) BETWEEN :fromDate AND :toDate
""")
    Long totalOrders(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

}