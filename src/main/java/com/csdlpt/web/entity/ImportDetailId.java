package com.csdlpt.web.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ImportDetailId implements Serializable {

    @Column(name = "import_id", length = 50)
    private String importId;

    @Column(name = "product_id", length = 50)
    private String productId;
}
