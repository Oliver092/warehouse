package com.example.warehouse.dto;

import lombok.Data;

@Data
public class AisleDTO {
    private Long id;
    private String name;
    private Long hallId;
    private String hallName;
    private int shelfCount;
}