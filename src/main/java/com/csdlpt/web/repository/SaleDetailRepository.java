package com.csdlpt.web.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.csdlpt.web.dto.TopProductDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.SaleDetail;
import com.csdlpt.web.entity.SaleDetailId;
import org.springframework.data.jpa.repository.Query;

public interface SaleDetailRepository extends JpaRepository<SaleDetail, SaleDetailId> {

    @EntityGraph(attributePaths = {"product", "product.category", "sale"})
    List<SaleDetail> findBySale_IdOrderByProduct_IdAsc(String saleId);

    @Query("""
        select coalesce(sum(sd.quantity * sd.salePrice),0)
        from SaleDetail sd
    """)
    BigDecimal getTotalRevenue();

    @Query("""
        select coalesce(sum(sd.quantity * sd.salePrice),0)
        from SaleDetail sd
        where cast(sd.sale.createdAt as date)=:date
    """)
    BigDecimal getRevenueByDate(LocalDate date);

    @Query(value = """
        select top 5 p.name as productName,
               sum(sd.quantity) as totalSold
        from SaleDetail sd
        join Product p on sd.product_id = p.id
        group by p.name
        order by totalSold desc
    """, nativeQuery = true)
    List<TopProductDto> getTopProducts();

}
