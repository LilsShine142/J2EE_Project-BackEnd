package com.example.j2ee_project.controller;

import com.example.j2ee_project.exception.ForbiddenException;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.UserDTO;
import com.example.j2ee_project.model.request.user.UserRequest;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing user accounts")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ResponseHandler responseHandler;

    // ==================== REGISTER - KHÔNG CẦN TOKEN ====================
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserDTO response = userService.createUser(userRequest);
        return responseHandler.responseCreated("Tạo người dùng thành công", response);
    }

    // ==================== CÁC ENDPOINT CẦN TOKEN + QUYỀN ====================
    // Helper method để extract token - giống hệt cách bạn đang làm ở HTTT_BE
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ForbiddenException("Thiếu token xác thực");
        }
        return authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader;
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "statusId", required = false) Integer statusId,
            @RequestParam(value = "roleId", required = false) Integer roleId) {

        try {
            String authToken = extractToken(token); // chỉ extract, không check quyền
            Map<String, Object> result = userService.getUsersPaginated(authToken, offset, limit, username, search, statusId, roleId);
            return responseHandler.responseSuccess("Lấy danh sách người dùng thành công", result);

        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return responseHandler.handleServerError("Đã xảy ra lỗi khi lấy danh sách người dùng");
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer userId) {

        try {
            String authToken = extractToken(token);
            UserDTO response = userService.getUserById(authToken, userId);
            return responseHandler.responseSuccess("Lấy thông tin người dùng thành công", response);

        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            return responseHandler.handleNotFound(e.getMessage());
        } catch (Exception e) {
            return responseHandler.handleServerError("Đã xảy ra lỗi khi lấy thông tin người dùng");
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer userId,
            @Valid @RequestBody UserDTO userDTO) {

        try {
            String authToken = extractToken(token);
            userDTO.setUserId(userId);
            UserDTO response = userService.updateUser(authToken, userId, userDTO);
            return responseHandler.responseSuccess("Cập nhật người dùng thành công", response);

        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            return responseHandler.handleNotFound(e.getMessage());
        } catch (Exception e) {
            return responseHandler.handleServerError("Đã xảy ra lỗi khi cập nhật người dùng");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer userId) {

        try {
            String authToken = extractToken(token);
            userService.deleteUser(authToken, userId);
            return responseHandler.responseSuccess("Xóa người dùng thành công", null);

        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            return responseHandler.handleNotFound(e.getMessage());
        } catch (Exception e) {
            return responseHandler.handleServerError("Đã xảy ra lỗi khi xóa người dùng");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@RequestHeader("Authorization") String token) {
        try {
            String authToken = extractToken(token); // hàm bạn đã có trong controller

            UserDTO userDTO = userService.getMyProfile(authToken);

            return responseHandler.responseSuccess("Lấy thông tin cá nhân thành công", userDTO);

        } catch (ForbiddenException e) {
            return responseHandler.responseError(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            return responseHandler.handleNotFound(e.getMessage());
        } catch (Exception e) {
            return responseHandler.handleServerError("Lỗi khi lấy thông tin cá nhân");
        }
    }
}