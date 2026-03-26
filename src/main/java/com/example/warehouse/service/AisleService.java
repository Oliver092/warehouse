package com.example.warehouse.service;

import com.example.warehouse.dto.AisleDTO;
import com.example.warehouse.entity.Aisle;
import com.example.warehouse.entity.Hall;
import com.example.warehouse.repository.AisleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AisleService {

    private final AisleRepository aisleRepository;
    private final HallService hallService;

    public List<AisleDTO> getAll() {
        return aisleRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public AisleDTO getById(Long id) {
        return toDTO(findById(id));
    }

    public AisleDTO create(Long hallId, Aisle aisle) {
        Hall hall = hallService.findById(hallId);

        if (aisleRepository.existsByHallIdAndName(hallId, aisle.getName())) {
            throw new RuntimeException(
                    "Aisle '" + aisle.getName() + "' already exists in this hall"
            );
        }

        aisle.setHall(hall);
        return toDTO(aisleRepository.save(aisle));}

    public void delete(Long id) {
        aisleRepository.deleteById(id);
    }

    public Aisle findById(Long id) {
        return aisleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aisle not found: " + id));
    }

    private AisleDTO toDTO(Aisle aisle) {
        AisleDTO dto = new AisleDTO();
        dto.setId(aisle.getId());
        dto.setName(aisle.getName());
        dto.setHallId(aisle.getHall().getId());
        dto.setHallName(aisle.getHall().getName());
        dto.setShelfCount(aisle.getShelves().size());
        return dto;
    }
}