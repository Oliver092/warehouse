package com.example.warehouse.controller;

import com.example.warehouse.dto.ProductDTO;
import com.example.warehouse.entity.Product;
import com.example.warehouse.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDTO> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping("/shelf/{shelfId}")
    public ResponseEntity<ProductDTO> create(@PathVariable Long shelfId, @RequestBody Product product) {
        return ResponseEntity.ok(productService.create(shelfId, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assign-shelf/{shelfId}")
    public ResponseEntity<ProductDTO> assignToShelf(
            @PathVariable Long id,
            @PathVariable Long shelfId) {
        return ResponseEntity.ok(productService.assignToShelf(id, shelfId));
    }
}
