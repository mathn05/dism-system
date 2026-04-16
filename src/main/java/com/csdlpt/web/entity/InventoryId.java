package com.csdlpt.web.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
public class InventoryId implements Serializable {

    private String stationId;
    private String productId;
}
