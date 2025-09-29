
package com.example.j2ee_project.service.table;

import com.example.j2ee_project.entity.RestaurantTable;
import java.time.LocalDateTime;
import java.util.Map;

public interface TableServiceInterface {
    Map<String, Object> getAvailableTables(LocalDateTime bookingDate, LocalDateTime startTime,
            LocalDateTime endTime, Integer numberOfGuests, int offset, int limit);
}