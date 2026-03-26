package com.example.warehouse.service;

import com.example.warehouse.dto.ProductDTO;
import com.example.warehouse.entity.Product;
import com.example.warehouse.entity.Shelf;
import com.example.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShelfService shelfService;

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
                throw new RuntimeException(
                        "Shelf " + shelf.getCode() + " capacity exceeded. " +
                                "Available space: " + (shelf.getMaxCapacity() - currentLoad)
                );
            }
        }

        // Check if shelf already has a different SKU
        if (!shelf.getProducts().isEmpty()) {
            String existingSku = shelf.getProducts().get(0).getSku();
            if (existingSku != null && !existingSku.equals(product.getSku())) {
                throw new RuntimeException(
                        "Shelf " + shelf.getCode() + " already contains SKU: "
                                + existingSku + ". Cannot mix SKUs on the same shelf."
                );
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
        return toDTO(productRepository.save(product));
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
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
            throw new RuntimeException(
                    "Product is already on shelf: " + newShelf.getCode()
            );
        }

        // Check SKU conflict
        if (!newShelf.getProducts().isEmpty()) {
            String existingSku = newShelf.getProducts().get(0).getSku();
            if (existingSku != null && !existingSku.equals(product.getSku())) {
                throw new RuntimeException(
                        "Cannot move to shelf " + newShelf.getCode() +
                                " — already contains different SKU: " + existingSku
                );
            }
        }

        // Check capacity
        if (newShelf.getMaxCapacity() != null) {
            int currentLoad = newShelf.getCurrentLoad();
            int incoming = product.getQuantity() != null ? product.getQuantity() : 0;
            if (currentLoad + incoming > newShelf.getMaxCapacity()) {
                throw new RuntimeException(
                        "Shelf " + newShelf.getCode() + " capacity exceeded. " +
                                "Available space: " + (newShelf.getMaxCapacity() - currentLoad)
                );
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
            throw new RuntimeException(
                    "Cannot reduce quantity below 0. Current: " + product.getQuantity()
            );
        }

        product.setQuantity(newQuantity);
        Product saved = productRepository.save(product);

        // Check reorder threshold after update
        if (saved.getReorderThreshold() != null &&
                saved.getQuantity() <= saved.getReorderThreshold()) {
            // In a real system this would trigger a notification/task
            // For now we just log it
            System.out.println("LOW STOCK ALERT: " + saved.getName() +
                    " (SKU: " + saved.getSku() + ") quantity is " +
                    saved.getQuantity() + " — reorder threshold: " +
                    saved.getReorderThreshold());
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
