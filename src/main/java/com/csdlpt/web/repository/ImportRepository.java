package com.csdlpt.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.ImportEntity;

public interface ImportRepository extends JpaRepository<ImportEntity, String> {

    @EntityGraph(attributePaths = {"user", "supplier", "station"})
    List<ImportEntity> findByStation_IdOrderByCreatedAtDescIdDesc(String stationId);
}
