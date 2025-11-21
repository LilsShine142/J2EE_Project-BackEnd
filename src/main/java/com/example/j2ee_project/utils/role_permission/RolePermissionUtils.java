package com.example.j2ee_project.utils.role_permission;

import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.utils.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePermissionUtils {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * Kiểm tra xem user có quyền truy cập hay không
     * @param token JWT token (Bearer token)
     * @param permissionCode Mã quyền cần kiểm tra
     * @return true nếu có quyền, false nếu không
     */
    public boolean hasPermission(String token, String permissionCode) {
        try {
            // Loại bỏ "Bearer " nếu có
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Validate token và lấy email
            if (!jwtTokenProvider.verifyToken(token)) {
                return false;
            }

            String email = jwtTokenProvider.getEmailFromToken(token);

            // Lấy user từ database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

            // Check if role and rolePermissions are not null
            if (user.getRole() == null || user.getRole().getRolePermissions() == null) {
                return false;
            }

            // Lấy tất cả permissions của user thông qua role
            Set<String> userPermissions = user.getRole().getRolePermissions().stream()
                    .map(rp -> rp.getPermission().getPermissionName())
                    .collect(Collectors.toSet());

            // Kiểm tra xem user có quyền này không
            return userPermissions.contains(permissionCode);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lấy tất cả permissions của user từ token
     * @param token JWT token (Bearer token)
     * @return Set các mã quyền của user
     */
    public Set<String> getUserPermissions(String token) {
        try {
            // Loại bỏ "Bearer " nếu có
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Validate token và lấy email
            if (!jwtTokenProvider.verifyToken(token)) {
                return Set.of();
            }

            String email = jwtTokenProvider.getEmailFromToken(token);

            // Lấy user từ database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

            // Check if role and rolePermissions are not null
            if (user.getRole() == null || user.getRole().getRolePermissions() == null) {
                return Set.of();
            }

            // Lấy tất cả permissions của user thông qua role
            return user.getRole().getRolePermissions().stream()
                    .map(rp -> rp.getPermission().getPermissionName())
                    .collect(Collectors.toSet());

        } catch (Exception e) {
            return Set.of();
        }
    }

    /**
     * Lấy User ID từ token
     * @param token JWT token (Bearer token)
     * @return User ID
     */
    public Integer getUserIdFromToken(String token) {
        try {
            // Loại bỏ "Bearer " nếu có
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Validate token và lấy email
            if (!jwtTokenProvider.verifyToken(token)) {
                return null;
            }

            String email = jwtTokenProvider.getEmailFromToken(token);

            // Lấy user từ database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

            return user.getUserID();

        } catch (Exception e) {
            return null;
        }
    }
}
