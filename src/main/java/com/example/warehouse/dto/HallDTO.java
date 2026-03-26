package com.example.warehouse.dto;

import lombok.Data;

@Data
public class HallDTO {
    private Long id;
    private String name;
    private String description;
    private int aisleCount;
}