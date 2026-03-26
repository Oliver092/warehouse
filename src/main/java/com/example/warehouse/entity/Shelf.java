package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shelves", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"aisle_id", "code"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shelf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // e.g. "S2-B" (shelf 2, level B) - possible change, depending on business logic

    private Integer maxCapacity;

    @ManyToOne
    @JoinColumn(name = "aisle_id", nullable = false)
    private Aisle aisle;

    @OneToMany(mappedBy = "shelf", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    public int getCurrentLoad() {
        return products.stream()
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();
    }
}