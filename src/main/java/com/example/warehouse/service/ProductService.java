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

        // If same SKU already on this shelf, just add to quantity
        Optional<Product> existing = productRepository
                .findByShelfIdAndSku(shelfId, product.getSku());

        if (existing.isPresent()) {
            Product p = existing.get();
            p.setQuantity(p.getQuantity() + product.getQuantity());
            return toDTO(productRepository.save(p));
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
