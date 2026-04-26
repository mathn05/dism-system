package com.csdlpt.web.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.ImportEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImportRepository extends JpaRepository<ImportEntity, String> {

    @EntityGraph(attributePaths = {"user", "supplier", "station"})
    List<ImportEntity> findByStation_IdOrderByCreatedAtDescIdDesc(String stationId);


    @Query("""
                SELECT SUM(d.quantity)
                FROM ImportEntity i
                JOIN ImportDetail d ON d.importEntity.id = i.id
                GROUP BY CAST(i.createdAt AS date)
                ORDER BY CAST(i.createdAt AS date)
            """)
    List<Long> importData();

    @Query("""
                SELECT CAST(i.createdAt AS date)
                FROM ImportEntity i
                GROUP BY CAST(i.createdAt AS date)
                ORDER BY CAST(i.createdAt AS date)
            """)
    List<Object> costLabels();

    @Query("""
                SELECT SUM(d.quantity * d.importPrice)
                FROM ImportEntity i
                JOIN ImportDetail d ON d.importEntity.id = i.id
                GROUP BY CAST(i.createdAt AS date)
                ORDER BY CAST(i.createdAt AS date)
            """)
    List<Long> costData();

    @Query("""
                SELECT COALESCE(SUM(d.quantity * d.importPrice), 0)
                FROM ImportEntity i
                JOIN ImportDetail d ON d.importEntity.id = i.id
            """)
    Long totalCost();

    @Query("""
                SELECT i.station.name, SUM(d.quantity)
                                       FROM ImportEntity i
                                       JOIN ImportDetail d ON d.importEntity.id = i.id
                                       WHERE i.station.id = :stationId
                                       GROUP BY i.station.name
            """)
    List<Object[]> importByStation(@Param("stationId") String stationId);

    @Query("""
                SELECT i.station.name, SUM(d.quantity)
                FROM ImportEntity i
                JOIN ImportDetail d ON d.importEntity.id = i.id
                GROUP BY i.station.name
            """)
    List<Object[]> importByStation();

    @Query("""
                SELECT CAST(i.createdAt AS date)
                FROM ImportEntity i
                WHERE i.station.id = :stationId
                GROUP BY CAST(i.createdAt AS date)
                ORDER BY CAST(i.createdAt AS date)
            """)
    List<Object[]> importLabels(@Param("stationId") String stationId);

    @Query("""
                SELECT SUM(d.quantity)
                FROM ImportEntity i
                JOIN ImportDetail d ON d.importEntity.id = i.id
                WHERE i.station.id = :stationId
                GROUP BY CAST(i.createdAt AS date)
                ORDER BY CAST(i.createdAt AS date)
            """)
    List<Long> importData(@Param("stationId") String stationId);

    @Query("""
                SELECT COALESCE(SUM(d.quantity * d.importPrice), 0)
                FROM ImportEntity i
                JOIN ImportDetail d ON d.importEntity.id = i.id
                WHERE i.station.id = :stationId
            """)
    Long totalCostByStation(@Param("stationId") String stationId);

    @Query("""
                SELECT CAST(i.createdAt AS date)
                FROM ImportEntity i
                WHERE i.station.id = :stationId
                GROUP BY CAST(i.createdAt AS date)
                ORDER BY CAST(i.createdAt AS date)
            """)
    List<Object[]> costLabels(@Param("stationId") String stationId);

    @Query("""
                SELECT SUM(d.quantity * d.importPrice)
                FROM ImportEntity i
                JOIN ImportDetail d ON d.importEntity.id = i.id
                WHERE i.station.id = :stationId
                GROUP BY CAST(i.createdAt AS date)
                ORDER BY CAST(i.createdAt AS date)
            """)
    List<Long> costData(@Param("stationId") String stationId);

    @Query(value = """
                SELECT COALESCE(SUM(d.quantity), 0)
                FROM [Import] i
                JOIN [ImportDetail] d ON i.id = d.import_id
                WHERE i.station_id = :stationId
                  AND CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
                GROUP BY CAST(i.created_at AS date)
                ORDER BY CAST(i.created_at AS date)
            """, nativeQuery = true)
    List<Long> importData(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT COALESCE(SUM(d.quantity), 0)
                FROM [Import] i
                JOIN [ImportDetail] d ON i.id = d.import_id
                WHERE CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
                GROUP BY CAST(i.created_at AS date)
                ORDER BY CAST(i.created_at AS date)
            """, nativeQuery = true)
    List<Long> importData(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT CAST(i.created_at AS date)
                FROM [Import] i
                WHERE i.station_id = :stationId
                  AND CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
                GROUP BY CAST(i.created_at AS date)
                ORDER BY CAST(i.created_at AS date)
            """, nativeQuery = true)
    List<Object[]> importLabels(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT CAST(i.created_at AS date)
                FROM [Import] i
                WHERE CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
                GROUP BY CAST(i.created_at AS date)
                ORDER BY CAST(i.created_at AS date)
            """, nativeQuery = true)
    List<Object[]> importLabels(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT COALESCE(SUM(d.quantity * d.import_price), 0)
                FROM [Import] i
                JOIN [ImportDetail] d ON i.id = d.import_id
                WHERE i.station_id = :stationId
                  AND CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
            """, nativeQuery = true)
    Long totalCostByStation(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT COALESCE(SUM(d.quantity * d.import_price), 0)
                FROM [Import] i
                JOIN [ImportDetail] d ON i.id = d.import_id
                WHERE CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
            """, nativeQuery = true)
    Long totalCost(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT CAST(i.created_at AS date)
                FROM [Import] i
                WHERE i.station_id = :stationId
                  AND CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
                GROUP BY CAST(i.created_at AS date)
                ORDER BY CAST(i.created_at AS date)
            """, nativeQuery = true)
    List<Object[]> costLabels(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT CAST(i.created_at AS date)
                FROM [Import] i
                WHERE CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
                GROUP BY CAST(i.created_at AS date)
                ORDER BY CAST(i.created_at AS date)
            """, nativeQuery = true)
    List<Object> costLabels(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT COALESCE(SUM(d.quantity * d.import_price), 0)
                FROM [Import] i
                JOIN [ImportDetail] d ON i.id = d.import_id
                WHERE i.station_id = :stationId
                  AND CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
                GROUP BY CAST(i.created_at AS date)
                ORDER BY CAST(i.created_at AS date)
            """, nativeQuery = true)
    List<Long> costData(
            @Param("stationId") String stationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT COALESCE(SUM(d.quantity * d.import_price), 0)
                FROM [Import] i
                JOIN [ImportDetail] d ON i.id = d.import_id
                WHERE CAST(i.created_at AS date) BETWEEN :fromDate AND :toDate
                GROUP BY CAST(i.created_at AS date)
                ORDER BY CAST(i.created_at AS date)
            """, nativeQuery = true)
    List<Long> costData(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query(value = """
                SELECT CAST(i.created_at AS date)
                FROM [Import] i
                GROUP BY CAST(i.created_at AS date)
                ORDER BY CAST(i.created_at AS date)
            """, nativeQuery = true)
    List<Object> importLabels();

}

