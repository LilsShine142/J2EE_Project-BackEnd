package com.example.j2ee_project.service.tableTypes;

import com.example.j2ee_project.model.dto.TableTypeDTO;
import com.example.j2ee_project.model.request.table.TableTypesRequest;
import org.springframework.data.domain.Page;

public interface TableTypeServiceInterface {
    TableTypeDTO createTableType(TableTypesRequest tableTypeRequest);

    Page<TableTypeDTO> getAllTableTypes(int offset, int limit, String search);

    TableTypeDTO getTableTypeById(Integer tableTypeId);

    TableTypeDTO updateTableType(Integer tableTypeId, TableTypesRequest tableTypeRequest);

    void deleteTableType(Integer tableTypeId);
}