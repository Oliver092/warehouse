package com.example.warehouse.controller;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.example.warehouse.dto.ProductDTO;
import com.example.warehouse.entity.Product;
import com.example.warehouse.entity.ProductDocument;
import com.example.warehouse.service.ProductSearchService;
import com.example.warehouse.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final ProductSearchService productSearchService;

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

    @PatchMapping("/{id}/move/{shelfId}")
    public ResponseEntity<ProductDTO> moveToShelf(
            @PathVariable Long id,
            @PathVariable Long shelfId) {
        return ResponseEntity.ok(productService.moveToShelf(id, shelfId));
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<ProductDTO> updateQuantity(
            @PathVariable Long id,
            @RequestParam int change) {
        return ResponseEntity.ok(productService.updateQuantity(id, change));
    }

    // Get all low stock products - managers can monitor this
    @GetMapping("/low-stock")
    public List<ProductDTO> getLowStock() {
        return productService.getLowStockProducts();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDocument>> search(@RequestParam String q) {
        return ResponseEntity.ok(productSearchService.search(q));
    }

    @GetMapping("/search/fuzzy")
    public ResponseEntity<List<ProductDocument>> fuzzySearch(@RequestParam String q) {
        return ResponseEntity.ok(productSearchService.fuzzySearch(q));
    }

    @PostMapping("/reindex")
    public ResponseEntity<String> reindex() {
        productService.reindexAll();
        return ResponseEntity.ok("Reindex triggered successfully");
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        return ResponseEntity.ok(productSearchService.getStatisticsByHall());
    }
}
