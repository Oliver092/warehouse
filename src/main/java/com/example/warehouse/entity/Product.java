package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String sku; // Stock Keeping Unit - unique product code

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "shelf_id")
    private Shelf shelf;
}