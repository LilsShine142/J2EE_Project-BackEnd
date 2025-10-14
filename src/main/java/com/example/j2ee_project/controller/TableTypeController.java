package com.example.j2ee_project.controller;

import com.example.j2ee_project.model.dto.TableTypeDTO;
import com.example.j2ee_project.model.request.table.TableTypesRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.tableTypes.TableTypeServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tabletypes")
public class TableTypeController {

    private final TableTypeServiceInterface tableTypeService;
    private final ResponseHandler responseHandler;

    @Autowired
    public TableTypeController(TableTypeServiceInterface tableTypeService, ResponseHandler responseHandler) {
        this.tableTypeService = tableTypeService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTableType(@Valid @RequestBody TableTypesRequest request) {
        TableTypeDTO response = tableTypeService.createTableType(request);
        return responseHandler.responseCreated("Tạo loại bàn thành công", response);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllTableTypes(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        Page<TableTypeDTO> tableTypePage = tableTypeService.getAllTableTypes(offset, limit, search);
        return responseHandler.responseSuccess("Lấy danh sách loại bàn thành công", tableTypePage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTableTypeById(@PathVariable Integer id) {
        TableTypeDTO response = tableTypeService.getTableTypeById(id);
        return responseHandler.responseSuccess("Lấy thông tin loại bàn thành công", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTableType(@PathVariable Integer id, @Valid @RequestBody TableTypesRequest request) {
        TableTypeDTO response = tableTypeService.updateTableType(id, request);
        return responseHandler.responseSuccess("Cập nhật loại bàn thành công", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTableType(@PathVariable Integer id) {
        tableTypeService.deleteTableType(id);
        return responseHandler.responseSuccess("Xóa loại bàn thành công", null);
    }
}