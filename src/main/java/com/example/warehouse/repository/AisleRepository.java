package com.example.warehouse.repository;

import com.example.warehouse.entity.Aisle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AisleRepository extends JpaRepository<Aisle, Long> {
    boolean existsByHallIdAndName(Long hallId, String name);
}