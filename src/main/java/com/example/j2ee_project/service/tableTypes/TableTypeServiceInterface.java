package com.example.j2ee_project.service.tableTypes;

import com.example.j2ee_project.model.dto.TableTypeDTO;
import com.example.j2ee_project.model.request.table.TableTypesRequest;
import org.springframework.data.domain.Page;

public interface TableTypeServiceInterface {
    TableTypeDTO createTableType(String token, TableTypesRequest tableTypeRequest);

    Page<TableTypeDTO> getAllTableTypes(String token, int offset, int limit, String search);

    TableTypeDTO getTableTypeById(String token, Integer tableTypeId);

    TableTypeDTO updateTableType(String token, Integer tableTypeId, TableTypesRequest tableTypeRequest);

    void deleteTableType(String token, Integer tableTypeId);
}