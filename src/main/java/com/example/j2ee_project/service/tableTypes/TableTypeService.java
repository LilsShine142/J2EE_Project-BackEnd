package com.example.j2ee_project.service.tableTypes;

import com.example.j2ee_project.entity.TableType;
import com.example.j2ee_project.exception.ForbiddenException;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.TableTypeDTO;
import com.example.j2ee_project.model.request.table.TableTypesRequest;
import com.example.j2ee_project.repository.TableTypeRepository;
import com.example.j2ee_project.utils._enum.EPermission;
import com.example.j2ee_project.utils.role_permission.RolePermissionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TableTypeService implements TableTypeServiceInterface {

    private final TableTypeRepository tableTypeRepository;
    private final RolePermissionUtils rolePermissionUtils;

    @Override
    @Transactional
    public TableTypeDTO createTableType(String token, TableTypesRequest tableTypeRequest) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để tạo loại bàn!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.CREATE_TABLE_TYPE.getCode())) {
            throw new ForbiddenException("Bạn không có quyền tạo loại bàn!");
        }
        TableType tableType = new TableType();
        tableType.setTypeName(tableTypeRequest.getTypeName());
        tableType.setCapacity(tableTypeRequest.getCapacity());
        tableType.setCreatedAt(LocalDateTime.now());
        tableType.setUpdatedAt(LocalDateTime.now());

        TableType savedTableType = tableTypeRepository.save(tableType);
        return mapToTableTypeDTO(savedTableType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TableTypeDTO> getAllTableTypes(String token, int offset, int limit, String search) {
        if (token != null && !rolePermissionUtils.hasPermission(token, EPermission.VIEW_TABLE_TYPE.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem danh sách loại bàn!");
        }
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<TableType> tableTypePage = tableTypeRepository.findByTypeNameContainingIgnoreCase(search, pageable);
        return tableTypePage.map(this::mapToTableTypeDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public TableTypeDTO getTableTypeById(String token, Integer tableTypeId) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để xem thông tin loại bàn!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.VIEW_TABLE_TYPE.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xem thông tin loại bàn!");
        }
        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại bàn với ID: " + tableTypeId));
        return mapToTableTypeDTO(tableType);
    }

    @Override
    @Transactional
    public TableTypeDTO updateTableType(String token, Integer tableTypeId, TableTypesRequest tableTypeRequest) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để cập nhật loại bàn!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.UPDATE_TABLE_TYPE.getCode())) {
            throw new ForbiddenException("Bạn không có quyền cập nhật loại bàn!");
        }
        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại bàn với ID: " + tableTypeId));

        tableType.setTypeName(tableTypeRequest.getTypeName());
        tableType.setCapacity(tableTypeRequest.getCapacity());
        tableType.setUpdatedAt(LocalDateTime.now());

        TableType updatedTableType = tableTypeRepository.save(tableType);
        return mapToTableTypeDTO(updatedTableType);
    }

    @Override
    @Transactional
    public void deleteTableType(String token, Integer tableTypeId) {
        if (token == null) {
            throw new ForbiddenException("Cần đăng nhập để xóa loại bàn!");
        }
        if (!rolePermissionUtils.hasPermission(token, EPermission.DELETE_TABLE_TYPE.getCode())) {
            throw new ForbiddenException("Bạn không có quyền xóa loại bàn!");
        }

        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại bàn với ID: " + tableTypeId));
        tableTypeRepository.delete(tableType);
    }

    private TableTypeDTO mapToTableTypeDTO(TableType tableType) {
        TableTypeDTO tableTypeDTO = new TableTypeDTO();
        tableTypeDTO.setTableTypeID(tableType.getTableTypeID());
        tableTypeDTO.setTypeName(tableType.getTypeName());
        tableTypeDTO.setCapacity(tableType.getCapacity());
        tableTypeDTO.setCreatedAt(tableType.getCreatedAt());
        tableTypeDTO.setUpdatedAt(tableType.getUpdatedAt());
        return tableTypeDTO;
    }
}