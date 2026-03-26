package com.example.warehouse.repository;

import com.example.warehouse.entity.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    boolean existsByAisleIdAndCode(Long aisleId, String code);
}
