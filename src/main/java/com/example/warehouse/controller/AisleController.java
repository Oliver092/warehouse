package com.example.warehouse.controller;

import com.example.warehouse.dto.AisleDTO;
import com.example.warehouse.entity.Aisle;
import com.example.warehouse.service.AisleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/aisles")
@RequiredArgsConstructor
public class AisleController {

    private final AisleService aisleService;

    @GetMapping
    public List<AisleDTO> getAll() {
        return aisleService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AisleDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(aisleService.getById(id));
    }

    @PostMapping("/hall/{hallId}")
    public ResponseEntity<AisleDTO> create(@PathVariable Long hallId, @RequestBody Aisle aisle) {
        return ResponseEntity.ok(aisleService.create(hallId, aisle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        aisleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}