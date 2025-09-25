package com.example.j2ee_project.controller;

import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.UserDTO;
import com.example.j2ee_project.model.request.user.UserRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.user.UserService;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final ResponseHandler responseHandler;

    @Autowired
    public UserController(UserService userService, ResponseHandler responseHandler) {
        this.userService = userService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserDTO response = userService.createUser(userRequest);
        return responseHandler.responseCreated("Tạo người dùng thành công", response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "roleId", required = false) Integer roleId) {
        try {
            Map<String, Object> result = userService.getUsersPaginated(offset, limit, username, email, status, roleId);
            return responseHandler.responseSuccess("Lấy danh sách người dùng thành công", result);
        } catch (Exception e) {
            e.printStackTrace();
            return responseHandler.responseError("Đã xảy ra lỗi khi lấy danh sách người dùng",
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Integer userId) {
        try {
            UserDTO response = userService.getUserById(userId);
            return responseHandler.responseSuccess("Lấy thông tin người dùng thành công", response);
        } catch (ResourceNotFoundException ex) {
            return responseHandler.handleNotFound(ex.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Integer userId, @Valid @RequestBody UserDTO userDTO) {
        userDTO.setUserId(userId); // đảm bảo id khớp path param
        UserDTO response = userService.updateUser(userId, userDTO);
        return responseHandler.responseSuccess("Cập nhật người dùng thành công", response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return responseHandler.responseSuccess("Xóa người dùng thành công", null);
    }
}
