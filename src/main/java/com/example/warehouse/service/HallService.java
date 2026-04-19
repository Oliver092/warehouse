package com.example.warehouse.service;

import com.example.warehouse.dto.HallDTO;
import com.example.warehouse.entity.Hall;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallRepository hallRepository;

    public List<HallDTO> getAll() {
        return hallRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public HallDTO getById(Long id) {
        return toDTO(findById(id));
    }

    public HallDTO create(Hall hall) {
        return toDTO(hallRepository.save(hall));
    }

    public HallDTO update(Long id, Hall updated) {
        Hall hall = findById(id);
        hall.setName(updated.getName());
        hall.setDescription(updated.getDescription());
        return toDTO(hallRepository.save(hall));
    }

    public void delete(Long id) {
        hallRepository.deleteById(id);
    }

    // Used internally by other services
    public Hall findById(Long id) {
        return hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", id));
    }

    private HallDTO toDTO(Hall hall) {
        HallDTO dto = new HallDTO();
        dto.setId(hall.getId());
        dto.setName(hall.getName());
        dto.setDescription(hall.getDescription());
        dto.setAisleCount(hall.getAisles().size());
        return dto;
    }
}
