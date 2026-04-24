package com.csdlpt.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.ImportDetail;
import com.csdlpt.web.entity.ImportDetailId;

public interface ImportDetailRepository extends JpaRepository<ImportDetail, ImportDetailId> {

    @EntityGraph(attributePaths = {"product", "product.category", "importEntity"})
    List<ImportDetail> findByImportEntity_IdOrderByProduct_IdAsc(String importId);
}
