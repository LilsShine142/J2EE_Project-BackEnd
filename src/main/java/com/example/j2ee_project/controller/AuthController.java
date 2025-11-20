package com.example.j2ee_project.controller;

import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.model.dto.UserDTO;
import com.example.j2ee_project.model.request.auth.LoginRequest;
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
            String token = jwtTokenProvider.generateToken(loginRequest.getEmail());

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
        public ResponseEntity<?> oauth2Callback(
                @PathVariable String provider,
                @RequestParam(required = false) String code,
                HttpServletRequest request) {

            if (code == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không có code"));
            }

            // Spring Security đã xử lý code → Authentication có sẵn
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (!(auth instanceof OAuth2AuthenticationToken token)) {
                return ResponseEntity.status(401).body(Map.of("message", "Xác thực thất bại"));
            }

            OAuth2User oAuth2User = token.getPrincipal();
            User user = userService.processOAuthUser(oAuth2User, provider);
            String jwt = jwtTokenProvider.generateToken(user.getEmail());

            Map<String, Object> data = Map.of(
                    "success", true,
                    "token", jwt,
                    "user", Map.of(
                            "userId", user.getUserID(),
                            "email", user.getEmail(),
                            "fullName", user.getFullName(),
                            "phoneNumber", user.getPhoneNumber(),
                            "roleId", user.getRoleId() != null ? user.getRoleId() : 3
                    )
            );

            return ResponseEntity.ok(data);
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
            String token = jwtTokenProvider.generateToken(user.getEmail());
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", Map.of(
                    "userId", user.getUserID(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName(),
                    "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    "roleId", user.getRoleId() != null ? user.getRoleId() : 3
            ));
            responseData.put("token", token);
            responseData.put("googleAttributes", oAuth2User.getAttributes());
            return responseHandler.responseSuccess("Đăng nhập Google thành công", responseData);
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
            String token = jwtTokenProvider.generateToken(user.getEmail());
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", Map.of(
                    "userId", user.getUserID(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName(),
                    "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    "roleId", user.getRoleId() != null ? user.getRoleId() : 3
            ));
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
