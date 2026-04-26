package com.csdlpt.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.Station;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StationRepository extends JpaRepository<Station, String> {

    List<Station> findAllByOrderByIdAsc();


    @Query("select s.name from Station s")
    List<String> findAllNames();

    @Query("SELECT s FROM Station s WHERE s.headquarter = false")
    List<Station> findAllExceptHeadquarter();

    @Query("""
    SELECT s FROM Station s
    WHERE s.headquarter = false
       AND (
            :keyword IS NULL OR :keyword = ''
            OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
       )
""")
    List<Station> search(@Param("keyword") String keyword);
}

