package com.example.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aisles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hall_id", "name"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aisle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g. "Aisle 3"

    @ManyToOne
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @OneToMany(mappedBy = "aisle", cascade = CascadeType.ALL)
    private List<Shelf> shelves = new ArrayList<>();
}