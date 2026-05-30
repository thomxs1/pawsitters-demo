package com.pawsitters.service;

import com.pawsitters.model.AnimalType;
import com.pawsitters.model.Host;
import com.pawsitters.repository.HostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für HostService.
 */
class HostServiceTest {

    private HostRepository repository;
    private HostService service;

    @BeforeEach
    void setUp() {
        repository = mock(HostRepository.class);
        service = new HostService(repository);
    }

    @Test
    void create_endDateBeforeStart_throwsException() {
        Host host = new Host("Late Host", "late@example.com", "",
                Set.of(AnimalType.DOG),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 1),
                new BigDecimal("100"));
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.create(host));
    }

    @Test
    void findMatchingHosts_returnsOnlyMatchingHosts() {
        Host matching = new Host("Match", "m@example.com", "",
                Set.of(AnimalType.DOG),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 31),
                new BigDecimal("100"));
        Host wrongAnimal = new Host("WrongAnimal", "wa@example.com", "",
                Set.of(AnimalType.CAT),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 31),
                new BigDecimal("90"));
        Host wrongDate = new Host("WrongDate", "wd@example.com", "",
                Set.of(AnimalType.DOG),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 15),
                new BigDecimal("110"));

        when(repository.findAll()).thenReturn(List.of(matching, wrongAnimal, wrongDate));

        List<Host> result = service.findMatchingHosts(AnimalType.DOG,
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 25));

        assertEquals(1, result.size());
        assertEquals("Match", result.get(0).getName());
    }

    @Test
    void canAccommodate_periodPartiallyOutside_returnsFalse() {
        Host host = new Host("Test", "t@example.com", "",
                Set.of(AnimalType.DOG),
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 20),
                new BigDecimal("100"));

        // Anfragezeitraum endet nach Verfügbarkeit
        assertFalse(host.canAccommodate(AnimalType.DOG,
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 25)));
    }
}
