package com.example.warehouse;

import com.example.warehouse.dto.HallDTO;
import com.example.warehouse.entity.Hall;
import com.example.warehouse.repository.HallRepository;
import com.example.warehouse.service.HallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HallServiceTest {

    @Mock
    private HallRepository hallRepository;

    @InjectMocks
    private HallService hallService;

    private Hall hall;

    @BeforeEach
    void setUp() {
        hall = new Hall();
        hall.setId(1L);
        hall.setName("Hall A");
        hall.setDescription("Main hall");
        hall.setAisles(new ArrayList<>());
    }

    @Test
    void getAll_returnsListOfHallDTOs() {
        when(hallRepository.findAll()).thenReturn(List.of(hall));

        List<HallDTO> result = hallService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hall A");
        assertThat(result.get(0).getAisleCount()).isEqualTo(0);
    }

    @Test
    void getById_existingId_returnsHallDTO() {
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));

        HallDTO result = hallService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Hall A");
    }

    @Test
    void getById_nonExistingId_throwsException() {
        when(hallRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hallService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Hall not found");
    }

    @Test
    void create_savesAndReturnsHallDTO() {
        when(hallRepository.save(any(Hall.class))).thenReturn(hall);

        HallDTO result = hallService.create(hall);

        assertThat(result.getName()).isEqualTo("Hall A");
        verify(hallRepository, times(1)).save(hall);
    }

    @Test
    void update_existingHall_updatesAndReturnsDTO() {
        Hall updated = new Hall();
        updated.setName("Hall B");
        updated.setDescription("Updated description");

        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(hallRepository.save(any(Hall.class))).thenReturn(hall);

        HallDTO result = hallService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("Hall B");
        verify(hallRepository, times(1)).save(hall);
    }

    @Test
    void delete_callsRepositoryDeleteById() {
        doNothing().when(hallRepository).deleteById(1L);

        hallService.delete(1L);

        verify(hallRepository, times(1)).deleteById(1L);
    }
}

