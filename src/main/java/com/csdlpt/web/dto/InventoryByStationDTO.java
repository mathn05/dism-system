package com.csdlpt.web.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryByStationDTO {

    private String stationName;

    private Long totalStock;

    private Double percent;
}