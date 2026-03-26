package com.example.warehouse.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String sku;
    private Integer quantity;
    private Long shelfId;
    private String shelfCode;
    private String aisleName;
    private String hallName;
}