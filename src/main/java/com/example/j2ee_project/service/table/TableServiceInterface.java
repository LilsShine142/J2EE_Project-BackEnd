package com.example.j2ee_project.service.table;

import com.example.j2ee_project.model.dto.RestaurantTableDTO;
import com.example.j2ee_project.model.request.table.TableRequestDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface TableServiceInterface {
    RestaurantTableDTO createTable(TableRequestDTO tableRequestDTO);

    Page<RestaurantTableDTO> getAllTables(int offset, int limit, String search, Integer statusId, Integer numberOfGuests);

    Page<RestaurantTableDTO> getAvailableTables(int offset, int limit, LocalDateTime bookingDate, LocalDateTime startTime, LocalDateTime endTime, Integer numberOfGuests);

    RestaurantTableDTO getTableById(Integer tableId);

    RestaurantTableDTO updateTable(Integer tableId, TableRequestDTO tableRequestDTO);

    void deleteTable(Integer tableId);
}