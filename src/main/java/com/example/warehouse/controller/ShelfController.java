package com.example.warehouse.controller;

import com.example.warehouse.dto.ShelfDTO;
import com.example.warehouse.entity.Shelf;
import com.example.warehouse.service.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shelves")
@RequiredArgsConstructor
public class ShelfController {

    private final ShelfService shelfService;

    @GetMapping
    public List<ShelfDTO> getAll() {
        return shelfService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShelfDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shelfService.getById(id));
    }

    @PostMapping("/aisle/{aisleId}")
    public ResponseEntity<ShelfDTO> create(@PathVariable Long aisleId, @RequestBody Shelf shelf) {
        return ResponseEntity.ok(shelfService.create(aisleId, shelf));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shelfService.delete(id);
        return ResponseEntity.noContent().build();
    }
}