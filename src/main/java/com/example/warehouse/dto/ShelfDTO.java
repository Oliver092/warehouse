package com.example.warehouse.dto;

import lombok.Data;

@Data
public class ShelfDTO {
    private Long id;
    private String code;
    private Integer maxCapacity;
    private Long aisleId;
    private String aisleName;
    private int productCount;
}