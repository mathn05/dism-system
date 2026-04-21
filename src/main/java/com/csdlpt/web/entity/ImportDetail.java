package com.csdlpt.web.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ImportDetail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportDetail {

    @EmbeddedId
    private ImportDetailId id;

    @MapsId("importId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_id", nullable = false)
    private ImportEntity importEntity;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column
    private Integer quantity;

    @Column(name = "import_price", precision = 15, scale = 2)
    private BigDecimal importPrice;
}
