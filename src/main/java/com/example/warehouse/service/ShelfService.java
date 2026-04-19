package com.example.warehouse.service;

import com.example.warehouse.dto.ShelfDTO;
import com.example.warehouse.entity.Aisle;
import com.example.warehouse.entity.Shelf;
import com.example.warehouse.exception.DuplicateResourceException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.ShelfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShelfService {

    private final ShelfRepository shelfRepository;
    private final AisleService aisleService;

    public List<ShelfDTO> getAll() {
        return shelfRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ShelfDTO getById(Long id) {
        return toDTO(findById(id));
    }

    public ShelfDTO create(Long aisleId, Shelf shelf) {
        Aisle aisle = aisleService.findById(aisleId);

        if (shelfRepository.existsByAisleIdAndCode(aisleId, shelf.getCode())) {
            throw new DuplicateResourceException("Shelf '" + shelf.getCode() + "' already exists in this aisle");

        }

        shelf.setAisle(aisle);
        return toDTO(shelfRepository.save(shelf));
    }

    public void delete(Long id) {
        shelfRepository.deleteById(id);
    }

    public Shelf findById(Long id) {
        return shelfRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shelf", id));
    }

    private ShelfDTO toDTO(Shelf shelf) {
        ShelfDTO dto = new ShelfDTO();
        dto.setId(shelf.getId());
        dto.setCode(shelf.getCode());
        dto.setMaxCapacity(shelf.getMaxCapacity());
        dto.setAisleId(shelf.getAisle().getId());
        dto.setAisleName(shelf.getAisle().getName());
        dto.setProductCount(shelf.getProducts().size());
        return dto;
    }
}
