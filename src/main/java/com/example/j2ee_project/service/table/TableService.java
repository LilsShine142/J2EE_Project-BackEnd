package com.example.j2ee_project.service.table;

import com.example.j2ee_project.entity.RestaurantTable;
import com.example.j2ee_project.entity.Status;
import com.example.j2ee_project.entity.TableType;
import com.example.j2ee_project.exception.DuplicateResourceException;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.RestaurantTableDTO;
import com.example.j2ee_project.model.request.table.TableRequestDTO;
import com.example.j2ee_project.repository.RestaurantTableRepository;
import com.example.j2ee_project.repository.TableTypeRepository;
import com.example.j2ee_project.repository.StatusRepository;
import com.example.j2ee_project.utils._enum.EStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService implements TableServiceInterface {

    private final RestaurantTableRepository restaurantTableRepository;
    private final TableTypeRepository tableTypeRepository;
    private final StatusRepository statusRepository;

    @Override
    @Transactional
    public RestaurantTableDTO createTable(TableRequestDTO tableRequestDTO) {
        if (restaurantTableRepository.existsByTableName(tableRequestDTO.getTableName())) {
            throw new DuplicateResourceException("Tên bàn đã tồn tại: " + tableRequestDTO.getTableName());
        }

        Status status = statusRepository.findById(tableRequestDTO.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy trạng thái với ID: " + tableRequestDTO.getStatusId()));

        TableType tableType = tableTypeRepository.findById(tableRequestDTO.getTableTypeID())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy loại bàn với ID: " + tableRequestDTO.getTableTypeID()));

        RestaurantTable table = new RestaurantTable();
        table.setTableName(tableRequestDTO.getTableName());
        table.setLocation(tableRequestDTO.getLocation());
        table.setTableType(tableType);
        table.setStatus(status);
        table.setCreatedAt(LocalDateTime.now());
        table.setUpdatedAt(LocalDateTime.now());

        RestaurantTable savedTable = restaurantTableRepository.save(table);
        return mapToTableDTO(savedTable);
    }

    @Override
    public Page<RestaurantTableDTO> getAllTables(int offset, int limit, String search, Integer statusId, Integer capacity) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;

        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);

        Page<RestaurantTable> tablePage = restaurantTableRepository.findByFilters(search, statusId, capacity, pageable);

        return tablePage.map(this::mapToTableDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantTableDTO> getAvailableTables(int offset, int limit, LocalDateTime bookingDate, LocalDateTime startTime, LocalDateTime endTime, Integer capacity) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;

        String availableStatus = EStatus.AVAILABLE.getName();
        List<String> excludedStatuses = Arrays.asList(EStatus.CANCELLED.getName(), EStatus.COMPLETED.getName());

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<RestaurantTable> page = restaurantTableRepository.findAvailableTables(
                availableStatus, capacity, startTime, endTime, excludedStatuses, pageable);

        return page.map(this::mapToTableDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantTableDTO getTableById(Integer tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + tableId));
        return mapToTableDTO(table);
    }

    @Override
    @Transactional
    public RestaurantTableDTO updateTable(Integer tableId, TableRequestDTO tableRequestDTO) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + tableId));

        if (tableRequestDTO.getTableName() != null &&
                !tableRequestDTO.getTableName().equals(table.getTableName()) &&
                restaurantTableRepository.existsByTableName(tableRequestDTO.getTableName())) {
            throw new DuplicateResourceException("Tên bàn đã tồn tại: " + tableRequestDTO.getTableName());
        }

        if (tableRequestDTO.getTableName() != null) {
            table.setTableName(tableRequestDTO.getTableName());
        }
        if (tableRequestDTO.getTableTypeID() != null) {
            TableType tableType = tableTypeRepository.findById(tableRequestDTO.getTableTypeID())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy loại bàn với ID: " + tableRequestDTO.getTableTypeID()));
            table.setTableType(tableType);
        }
        if (tableRequestDTO.getStatusId() != null) {
            Status status = statusRepository.findById(tableRequestDTO.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy trạng thái với ID: " + tableRequestDTO.getStatusId()));
            table.setStatus(status);
        }

        table.setUpdatedAt(LocalDateTime.now());
        RestaurantTable updatedTable = restaurantTableRepository.save(table);
        return mapToTableDTO(updatedTable);
    }

    @Override
    @Transactional
    public void deleteTable(Integer tableId) {
        if (!restaurantTableRepository.existsById(tableId)) {
            throw new ResourceNotFoundException("Không tìm thấy bàn với ID: " + tableId);
        }
        restaurantTableRepository.deleteById(tableId);
    }

    private RestaurantTableDTO mapToTableDTO(RestaurantTable table) {
        RestaurantTableDTO.RestaurantTableDTOBuilder builder = RestaurantTableDTO.builder()
                .tableID(table.getTableID())
                .tableName(table.getTableName())
                .location(table.getLocation())
                .tableTypeID(table.getTableType().getTableTypeID())
                .tableTypeName(table.getTableType().getTypeName())
                .createdAt(table.getCreatedAt())
                .updatedAt(table.getUpdatedAt())
                .statusId(table.getStatus().getStatusID())
                .capacity(table.getTableType().getCapacity());
        return builder.build();
    }
}