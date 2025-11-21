package com.example.j2ee_project.controller;

import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.model.dto.UserDTO;
import com.example.j2ee_project.model.request.auth.LoginRequest;
import com.example.j2ee_project.model.response.ResponseData;
import com.example.j2ee_project.model.response.ResponseHandler;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.service.user.UserService;
import com.example.j2ee_project.utils.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Management", description = "APIs for user authentication and login operations")
public class AuthController {

    private final UserService userService;
    private final ResponseHandler responseHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Autowired
    public AuthController(UserService userService,
            ResponseHandler responseHandler,
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager,
                          UserRepository userRepository) {
        this.userService = userService;
        this.responseHandler = responseHandler;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        // Kiểm tra validation
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return responseHandler.responseError(errorMessage, org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        String email = loginRequest.getEmail().trim();

        try {
            System.out.println("Login attempt for email: " + loginRequest.getEmail());

            // Tìm user bằng email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));

            // Xác thực (dùng email làm username)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword())
            );
            // Tạo JWT token
            String token = jwtTokenProvider.generateToken(user.getUserID().toString());

            // Lấy thông tin user
            UserDTO userDTO = userService.getUserByEmail(loginRequest.getEmail());

            return responseHandler.responseSuccess("Đăng nhập thành công", Map.of("user", userDTO, "token", token));

        } catch (DisabledException e) {
            return responseHandler.responseError("Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN);
        } catch (BadCredentialsException e) {
            return responseHandler.responseError("Mật khẩu không đúng", HttpStatus.UNAUTHORIZED);
        } catch (UsernameNotFoundException e) {
            return responseHandler.responseError("Email không tồn tại", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return responseHandler.responseError("Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // API Google Login
//    @GetMapping("/google")
//    public ResponseEntity<?> googleLogin() {
//        // Redirect thủ công đến OAuth2 endpoint mặc định
//        return ResponseEntity.status(HttpStatus.FOUND)
//                .header("Location", "/oauth2/authorization/google")
//                .build();
//    }
    @GetMapping("/google")
    public void googleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Spring sẽ tự động xử lý OAuth2 flow
        // Sau khi Google xác thực → gọi successHandler trong SecurityConfig
        // → successHandler sẽ redirect + mang token
        response.sendRedirect("/oauth2/authorization/google");
    }


    @GetMapping("/oauth2/callback/{provider}")
    public ResponseEntity<ResponseData> oauth2Callback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            HttpServletRequest request) {

        if (code == null || code.isBlank()) {
            return responseHandler.responseError("Không có code xác thực từ " + provider, HttpStatus.BAD_REQUEST);
        }

        // Lấy Authentication từ Spring Security (đã được xử lý tự động)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            return responseHandler.responseError("Xác thực OAuth2 thất bại", HttpStatus.UNAUTHORIZED);
        }

        OAuth2User oAuth2User = token.getPrincipal();

        try {
            // Xử lý user từ Google/Facebook → tạo hoặc lấy user trong DB
            User user = userService.processOAuthUser(oAuth2User, provider);

            // Tạo JWT token
            String jwt = jwtTokenProvider.generateToken(user.getUserID().toString());

            // Tạo UserDTO chi tiết giống như login thường
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserID());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName() != null ? user.getFullName() : "");

            // Chỉ thêm phoneNumber nếu không null và không rỗng
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                userData.put("phoneNumber", user.getPhoneNumber());
            }

            userData.put("roleId", user.getRole() != null ? user.getRole().getRoleID() : 1); // default USER
            userData.put("statusId", user.getStatus() != null ? user.getStatus().getStatusID() : null);
            userData.put("statusWork", user.getStatusWork() != null ? user.getStatusWork() : "");
            userData.put("totalSpent", user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO);
            userData.put("loyaltyPoints", user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0);
            userData.put("joinDate", user.getJoinDate());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("updatedAt", user.getUpdatedAt());

            // Tạo data chứa user + token
            Map<String, Object> responseDataMap = new HashMap<>();
            responseDataMap.put("user", userData);
            responseDataMap.put("token", jwt);

            // Trả về đúng format của ResponseHandler
            return responseHandler.responseSuccess("Đăng nhập thành công với " + provider.toUpperCase(), responseDataMap);

        } catch (Exception e) {
            e.printStackTrace();
            return responseHandler.responseError("Xử lý đăng nhập " + provider + " thất bại: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/google/success")
    public ResponseEntity<?> googleLoginSuccess(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return responseHandler.responseError("Authentication failed", HttpStatus.UNAUTHORIZED);
            }
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof OAuth2User)) {
                return responseHandler.responseError("Invalid principal type", HttpStatus.BAD_REQUEST);
            }
            OAuth2User oAuth2User = (OAuth2User) principal;
            User user = userService.processOAuthUser(oAuth2User, "Google");
            String token = jwtTokenProvider.generateToken(user.getUserID().toString());
            Map<String, Object> responseData = new HashMap<>();

            if (user.getPhoneNumber() != null) {
                responseData.put("user", Map.of(
                        "userId", user.getUserID(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "phoneNumber", user.getPhoneNumber(),
                        "roleId", user.getRole() != null ? user.getRole().getRoleID() : 0
                ));
            } else {
                responseData.put("user", Map.of(
                        "userId", user.getUserID(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "roleId", user.getRole() != null ? user.getRole().getRoleID() : 0
                ));
            }
            responseData.put("token", token);
            responseData.put("googleAttributes", oAuth2User.getAttributes());
            return responseHandler.responseSuccess("Đăng nhập thành công", Map.of("data", responseData, "token", token));
        } catch (DisabledException e) {
            return responseHandler.responseError("Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            System.err.println("Google login error: " + e.getMessage());
            e.printStackTrace();
            return responseHandler.responseError("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/facebook")
    public ResponseEntity<?> facebookLogin() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/oauth2/authorization/facebook")
                .build();
    }

    @GetMapping("/facebook/success")
    public ResponseEntity<?> facebookLoginSuccess(Authentication authentication) {
        try {
            System.out.println("Processing Facebook login success");
            if (authentication == null || !authentication.isAuthenticated()) {
                return responseHandler.responseError("Authentication failed", HttpStatus.UNAUTHORIZED);
            }
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof OAuth2User)) {
                return responseHandler.responseError("Invalid principal type", HttpStatus.BAD_REQUEST);
            }
            OAuth2User oAuth2User = (OAuth2User) principal;
            User user = userService.processOAuthUser(oAuth2User, "Facebook");
            String token = jwtTokenProvider.generateToken(user.getUserID().toString());
            Map<String, Object> responseData = new HashMap<>();

            if (user.getPhoneNumber() != null) {
                responseData.put("user", Map.of(
                        "userId", user.getUserID(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "phoneNumber", user.getPhoneNumber(),
                        "roleId", user.getRole() != null ? user.getRole().getRoleID() : 0
                ));
            } else {
                responseData.put("user", Map.of(
                        "userId", user.getUserID(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "roleId", user.getRole() != null ? user.getRole().getRoleID() : 0
                ));
            }
            responseData.put("token", token);
            responseData.put("facebookAttributes", oAuth2User.getAttributes()); // Trả về toàn bộ dữ liệu từ Facebook
            return responseHandler.responseSuccess("Đăng nhập Facebook thành công", responseData);
        } catch (DisabledException e) {
            return responseHandler.responseError("Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            System.err.println("Facebook login error: " + e.getMessage());
            e.printStackTrace();
            return responseHandler.responseError(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Facebook login error: " + e.getMessage());
            e.printStackTrace();
            return responseHandler.responseError("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Test endpoint để debug
    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestBody Map<String, String> request) {
        return responseHandler.responseSuccess("Test thành công", request);
    }
}
