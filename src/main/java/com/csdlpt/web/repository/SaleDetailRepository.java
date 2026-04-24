package com.csdlpt.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.SaleDetail;
import com.csdlpt.web.entity.SaleDetailId;

public interface SaleDetailRepository extends JpaRepository<SaleDetail, SaleDetailId> {

    @EntityGraph(attributePaths = {"product", "product.category", "sale"})
    List<SaleDetail> findBySale_IdOrderByProduct_IdAsc(String saleId);
}
