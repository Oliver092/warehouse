package com.example.warehouse;

import com.example.warehouse.entity.Aisle;
import com.example.warehouse.entity.Hall;
import com.example.warehouse.entity.Product;
import com.example.warehouse.entity.Shelf;
import com.example.warehouse.exception.InvalidOperationException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ShelfCapacityExceededException;
import com.example.warehouse.exception.SkuConflictException;
import com.example.warehouse.repository.ProductRepository;
import com.example.warehouse.service.ProductSearchService;
import com.example.warehouse.service.ProductService;
import com.example.warehouse.service.ShelfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShelfService shelfService;

    @Mock
    private ProductSearchService productSearchService;

    @InjectMocks
    private ProductService productService;

    private Shelf shelf;
    private Product product;

    @BeforeEach
    void setUp() {
        Aisle aisle = new Aisle();
        aisle.setName("Aisle 1");

        Hall hall = new Hall();
        hall.setName("Hall A");
        aisle.setHall(hall);

        shelf = new Shelf();
        shelf.setId(1L);
        shelf.setCode("S1-A");
        shelf.setMaxCapacity(100);
        shelf.setAisle(aisle);
        shelf.setProducts(new ArrayList<>());

        product = new Product();
        product.setId(1L);
        product.setName("Snickers");
        product.setSku("SNCK-001");
        product.setQuantity(10);
        product.setShelf(shelf);
    }

    @Test
    void create_validProduct_savesAndIndexes() {
        when(shelfService.findById(1L)).thenReturn(shelf);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.create(1L, product);

        verify(productRepository, times(1)).save(product);
        verify(productSearchService, times(1)).indexProduct(product);
    }

    @Test
    void create_capacityExceeded_throwsException() {
        shelf.setMaxCapacity(5);
        product.setQuantity(10);

        when(shelfService.findById(1L)).thenReturn(shelf);

        assertThatThrownBy(() -> productService.create(1L, product))
                .isInstanceOf(ShelfCapacityExceededException.class)
                .hasMessageContaining("S1-A");
    }

    @Test
    void create_skuConflict_throwsException() {
        Product existing = new Product();
        existing.setSku("DIFFERENT-SKU");
        shelf.setProducts(List.of(existing));

        product.setSku("SNCK-001");
        when(shelfService.findById(1L)).thenReturn(shelf);

        assertThatThrownBy(() -> productService.create(1L, product))
                .isInstanceOf(SkuConflictException.class)
                .hasMessageContaining("S1-A");
    }

    @Test
    void findById_nonExistingId_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void updateQuantity_validChange_updatesAndIndexes() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.updateQuantity(1L, 5);

        assertThat(product.getQuantity()).isEqualTo(15);
        verify(productSearchService, times(1)).indexProduct(product);
    }

    @Test
    void updateQuantity_belowZero_throwsInvalidOperationException() {
        product.setQuantity(3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateQuantity(1L, -10))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Cannot reduce quantity below 0");
    }

    @Test
    void delete_callsRepositoryAndRemovesFromIndex() {
        productService.delete(1L);

        verify(productRepository, times(1)).deleteById(1L);
        verify(productSearchService, times(1)).deleteFromIndex(1L);
    }

    @Test
    void moveToShelf_alreadyOnSameShelf_throwsInvalidOperationException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(shelfService.findById(1L)).thenReturn(shelf);

        assertThatThrownBy(() -> productService.moveToShelf(1L, 1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already on shelf");
    }
}