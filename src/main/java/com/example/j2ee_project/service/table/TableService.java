
package com.example.j2ee_project.service.table;

import com.example.j2ee_project.entity.RestaurantTable;
import com.example.j2ee_project.repository.RestaurantTableRepository;
import com.example.j2ee_project.utils._enum.EStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TableService implements TableServiceInterface {
    private final RestaurantTableRepository restaurantTableRepository;

    @Autowired
    public TableService(RestaurantTableRepository restaurantTableRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAvailableTables(LocalDateTime bookingDate, LocalDateTime startTime,
            LocalDateTime endTime, Integer numberOfGuests, int offset, int limit) {
        String availableStatus = EStatus.AVAILABLE.getName();
        List<String> excludedStatuses = Arrays.asList(
                EStatus.CANCELLED.getName(),
                EStatus.COMPLETED.getName());

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<RestaurantTable> page = restaurantTableRepository.findAvailableTables(availableStatus, numberOfGuests,
                startTime, endTime, excludedStatuses, pageable);

        List<RestaurantTable> availableTables = page.getContent();

        Map<String, Object> result = new HashMap<>();
        result.put("tables", availableTables);
        result.put("currentPage", page.getNumber());
        result.put("totalItems", page.getTotalElements());
        result.put("totalPages", page.getTotalPages());

        return result;
    }
}
