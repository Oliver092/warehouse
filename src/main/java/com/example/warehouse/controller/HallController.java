package com.example.warehouse.controller;

import com.example.warehouse.dto.HallDTO;
import com.example.warehouse.entity.Hall;
import com.example.warehouse.service.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @GetMapping
    public List<HallDTO> getAll() {
        return hallService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.getById(id));
    }

    @PostMapping
    public ResponseEntity<HallDTO> create(@RequestBody Hall hall) {
        return ResponseEntity.ok(hallService.create(hall));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HallDTO> update(@PathVariable Long id, @RequestBody Hall hall) {
        return ResponseEntity.ok(hallService.update(id, hall));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hallService.delete(id);
        return ResponseEntity.noContent().build();
    }
}