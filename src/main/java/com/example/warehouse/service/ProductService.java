package com.example.warehouse.service;

import com.example.warehouse.dto.ProductDTO;
import com.example.warehouse.entity.Product;
import com.example.warehouse.entity.Shelf;
import com.example.warehouse.exception.InvalidOperationException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ShelfCapacityExceededException;
import com.example.warehouse.exception.SkuConflictException;
import com.example.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShelfService shelfService;
    private final ProductSearchService productSearchService;

    public List<ProductDTO> getAll() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ProductDTO getById(Long id) {
        return toDTO(findById(id));
    }

    public ProductDTO create(Long shelfId, Product product) {
        Shelf shelf = shelfService.findById(shelfId);

        // Check capacity
        if (shelf.getMaxCapacity() != null) {
            int currentLoad = shelf.getCurrentLoad();
            int incoming = product.getQuantity() != null ? product.getQuantity() : 0;
            if (currentLoad + incoming > shelf.getMaxCapacity()) {
                throw new ShelfCapacityExceededException(shelf.getCode(), shelf.getMaxCapacity() - currentLoad);
            }
        }

        // Check if shelf already has a different SKU
        if (!shelf.getProducts().isEmpty()) {
            String existingSku = shelf.getProducts().get(0).getSku();
            if (existingSku != null && !existingSku.equals(product.getSku())) {
                throw new SkuConflictException(shelf.getCode(), existingSku);
            }

            // Same SKU - just add to quantity instead of creating new entry
            Optional<Product> existing = productRepository
                    .findByShelfIdAndSku(shelfId, product.getSku());
            if (existing.isPresent()) {
                Product p = existing.get();
                p.setQuantity(p.getQuantity() + product.getQuantity());
                return toDTO(productRepository.save(p));
            }
        }

        product.setShelf(shelf);
        Product saved = productRepository.save(product);
        try {
            productSearchService.indexProduct(saved);
        } catch (Exception e) {
            log.warn("Failed to index product {} in Elasticsearch: {}",
                    saved.getId(), e.getMessage());
        }
        return toDTO(saved);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
        productSearchService.deleteFromIndex(id);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public ProductDTO assignToShelf(Long productId, Long shelfId) {
        Product product = findById(productId);
        Shelf shelf = shelfService.findById(shelfId);
        product.setShelf(shelf);
        return toDTO(productRepository.save(product));
    }

    public ProductDTO moveToShelf(Long productId, Long newShelfId) {
        Product product = findById(productId);
        Shelf newShelf = shelfService.findById(newShelfId);

        // Already on this shelf
        if (product.getShelf() != null &&
                product.getShelf().getId().equals(newShelfId)) {
            throw new InvalidOperationException("Product is already on shelf: " + newShelf.getCode());
        }

        // Check SKU conflict
        if (!newShelf.getProducts().isEmpty()) {
            String existingSku = newShelf.getProducts().get(0).getSku();
            if (existingSku != null && !existingSku.equals(product.getSku())) {
                throw new SkuConflictException(newShelf.getCode(), existingSku);
            }
        }

        // Check capacity
        if (newShelf.getMaxCapacity() != null) {
            int currentLoad = newShelf.getCurrentLoad();
            int incoming = product.getQuantity() != null ? product.getQuantity() : 0;
            if (currentLoad + incoming > newShelf.getMaxCapacity()) {
                throw new ShelfCapacityExceededException(newShelf.getCode(), newShelf.getMaxCapacity() - currentLoad);
            }
        }

        product.setShelf(newShelf);
        return toDTO(productRepository.save(product));
    }

    public ProductDTO updateQuantity(Long productId, int quantityChange) {
        Product product = findById(productId);
        int currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
        int newQuantity = currentQuantity + quantityChange;

        if (newQuantity < 0) {
            throw new InvalidOperationException("Cannot reduce quantity below 0. Current: " + product.getQuantity());
        }

        product.setQuantity(newQuantity);
        Product saved = productRepository.save(product);
        productSearchService.indexProduct(saved);

        // Check reorder threshold after update
        if (saved.getReorderThreshold() != null &&
                saved.getQuantity() <= saved.getReorderThreshold()) {
            // In a real system this would trigger a notification/task
            // For now we just log it
            log.warn("LOW STOCK ALERT: {} (SKU: {}) quantity is {} — reorder threshold: {}", saved.getName(),
                    saved.getSku(), saved.getQuantity(), saved.getReorderThreshold());
        }

        return toDTO(saved);
    }

    public List<ProductDTO> getLowStockProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getReorderThreshold() != null &&
                        p.getQuantity() != null &&
                        p.getQuantity() <= p.getReorderThreshold())
                .map(this::toDTO)
                .toList();
    }

    public void reindexAll() {
        List<Product> all = productRepository.findAll();
        productSearchService.reindexAll(all);
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setQuantity(product.getQuantity());
        dto.setShelfId(product.getShelf().getId());
        dto.setShelfCode(product.getShelf().getCode());
        dto.setAisleName(product.getShelf().getAisle().getName());
        dto.setHallName(product.getShelf().getAisle().getHall().getName());
        return dto;
    }
}
