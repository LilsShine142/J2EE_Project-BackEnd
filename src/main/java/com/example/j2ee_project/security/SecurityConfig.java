//////package com.example.j2ee_project.security;
//////
//////import org.springframework.context.annotation.Bean;
//////import org.springframework.context.annotation.Configuration;
//////import org.springframework.security.authentication.AuthenticationManager;
//////import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//////import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//////import org.springframework.security.config.http.SessionCreationPolicy;
//////import org.springframework.security.core.userdetails.UserDetailsService;
//////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//////import org.springframework.security.crypto.password.PasswordEncoder;
//////import org.springframework.security.web.SecurityFilterChain;
//////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//////
//////@Configuration
//////@EnableWebSecurity
//////public class SecurityConfig {
//////
//////    private final UserDetailsService userDetailsService;
//////    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//////    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//////
//////    public SecurityConfig(UserDetailsService userDetailsService,
//////                          JwtAuthenticationFilter jwtAuthenticationFilter,
//////                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
//////        this.userDetailsService = userDetailsService;
//////        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//////        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
//////    }
//////
//////    @Bean
//////    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//////        http
//////                .csrf(csrf -> csrf.disable())
//////                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//////                .authorizeHttpRequests(auth -> auth
//////                        .requestMatchers("/api/auth/**", "/login/oauth2/**", "/oauth2/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
//////                        .requestMatchers("/api/users/register", "/api/auth/google/success").permitAll()
//////                        .requestMatchers("/api/public/**", "/api/payment/callback", "/api/payment/ipn").permitAll()
//////                        .anyRequest().authenticated()
//////                )
//////                .oauth2Login(oauth2 -> oauth2
//////                        .defaultSuccessUrl("/api/auth/google/success", true)
//////                        .failureUrl("/api/auth/error")
//////                )
//////                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//////                .authenticationProvider(authenticationProvider())
//////                .exceptionHandling(exceptions -> exceptions
//////                        .authenticationEntryPoint(jwtAuthenticationEntryPoint));
//////
//////        return http.build();
//////    }
//////
//////    @Bean
//////    public DaoAuthenticationProvider authenticationProvider() {
//////        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//////        authProvider.setUserDetailsService(userDetailsService);
//////        authProvider.setPasswordEncoder(passwordEncoder());
//////        return authProvider;
//////    }
//////
//////    @Bean
//////    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//////        return config.getAuthenticationManager();
//////    }
//////
//////    @Bean
//////    public PasswordEncoder passwordEncoder() {
//////        return new BCryptPasswordEncoder();
//////    }
//////}
////
////
////
////
////
////package com.example.j2ee_project.security;
////
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.security.authentication.AuthenticationManager;
////import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
////import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
////import org.springframework.security.config.http.SessionCreationPolicy;
////import org.springframework.security.core.GrantedAuthority;
////import org.springframework.security.core.authority.SimpleGrantedAuthority;
////import org.springframework.security.core.userdetails.UserDetailsService;
////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
////import org.springframework.security.crypto.password.PasswordEncoder;
////import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
////
////@Configuration
////@EnableWebSecurity
////public class SecurityConfig {
////
////    private final UserDetailsService userDetailsService;
////    private final JwtAuthenticationFilter jwtAuthenticationFilter;
////    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
////
////    public SecurityConfig(UserDetailsService userDetailsService,
////                          JwtAuthenticationFilter jwtAuthenticationFilter,
////                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
////        this.userDetailsService = userDetailsService;
////        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
////        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
////    }
////
////    @Bean
////    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
////        http
////                .csrf(csrf -> csrf.disable())
////                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
////                .authorizeHttpRequests(auth -> auth
////                        .requestMatchers("/api/auth/**", "/login/oauth2/**", "/oauth2/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
////                        .requestMatchers("/api/auth/google/success", "/api/auth/facebook/success", "/api/auth/error").permitAll()
////                        .requestMatchers("/api/public/**", "/api/payment/callback", "/api/payment/ipn").permitAll()
////                        .anyRequest().authenticated()
////                )
////                .oauth2Login(oauth2 -> oauth2
////                        .successHandler((request, response, authentication) -> {
////                            if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
////                                String provider = oauth2Token.getAuthorizedClientRegistrationId();
////                                System.out.println("OAuth2 provider: " + provider);
////                                switch (provider.toLowerCase()) {
////                                    case "facebook":
////                                        response.sendRedirect("/api/auth/facebook/success");
////                                        break;
////                                    case "google":
////                                        response.sendRedirect("/api/auth/google/success");
////                                        break;
////                                    default:
////                                        System.err.println("Unknown provider: " + provider);
////                                        response.sendRedirect("/api/auth/error");
////                                        break;
////                                }
////                            } else {
////                                System.err.println("Invalid authentication type: " + authentication.getClass().getName());
////                                response.sendRedirect("/api/auth/error");
////                            }
////                        })
////                        .failureUrl("/api/auth/error")
////                )
////                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
////                .authenticationProvider(authenticationProvider())
////                .exceptionHandling(exceptions -> exceptions
////                        .authenticationEntryPoint(jwtAuthenticationEntryPoint));
////
////        return http.build();
////    }
////
////    @Bean
////    public DaoAuthenticationProvider authenticationProvider() {
////        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
////        authProvider.setUserDetailsService(userDetailsService);
////        authProvider.setPasswordEncoder(passwordEncoder());
////        return authProvider;
////    }
////
////    @Bean
////    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
////        return config.getAuthenticationManager();
////    }
////
////    @Bean
////    public PasswordEncoder passwordEncoder() {
////        return new BCryptPasswordEncoder();
////    }
////}
//
//
//
//
//
//package com.example.j2ee_project.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final UserDetailsService userDetailsService;
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//
//    public SecurityConfig(UserDetailsService userDetailsService,
//                          JwtAuthenticationFilter jwtAuthenticationFilter,
//                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
//        this.userDetailsService = userDetailsService;
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // CORS
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                // CSRF
//                .csrf(csrf -> csrf.disable())
//                // Session
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//                // Auth Rules
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/api/auth/**",
//                                "/api/users/register",
//                                "/login/oauth2/**", "/oauth2/**",
//                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
//                        ).permitAll()
//                        .requestMatchers(
//                                "/api/auth/google/success",
//                                "/api/auth/facebook/success",
//                                "/api/auth/error"
//                        ).permitAll()
//                        .requestMatchers("/api/public/**", "/api/payment/callback", "/api/payment/ipn").permitAll()
//                        .anyRequest().authenticated()
//                )
//                // OAuth2 Login
//                .oauth2Login(oauth2 -> oauth2
//                        .successHandler((request, response, authentication) -> {
//                            if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
//                                String provider = oauth2Token.getAuthorizedClientRegistrationId();
//                                switch (provider.toLowerCase()) {
//                                    case "facebook" -> response.sendRedirect("/api/auth/facebook/success");
//                                    case "google" -> response.sendRedirect("/api/auth/google/success");
//                                    default -> response.sendRedirect("/api/auth/error");
//                                }
//                            } else {
//                                response.sendRedirect("/api/auth/error");
//                            }
//                        })
//                        .failureUrl("/api/auth/error")
//                )
//                // JWT Filter
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                // Auth Provider
//                .authenticationProvider(authenticationProvider())
//                // Exception
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
//                );
//
//        return http.build();
//    }
//
//    // CORS CONFIG
//    @Bean
//    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.setAllowedOriginPatterns(List.of(
//                "http://localhost:5173",     // Vite dev
//                "http://127.0.0.1:5173",
//                "http://localhost:3000"      // React dev (nếu dùng)
//        ));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
//
//    @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}















package com.example.j2ee_project.security;

import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.service.user.UserService;
import com.example.j2ee_project.utils.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          UserService userService,
                          JwtTokenProvider jwtTokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF
                .csrf(csrf -> csrf.disable())

                // Session: IF_REQUIRED cho OAuth2
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // Phân quyền
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users/register",
                                "/login/oauth2/**",
                                "/oauth2/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/public/**", "/api/payment/callback", "/api/payment/ipn").permitAll()
                        .anyRequest().authenticated()
                )

                // OAuth2 Login: TRẢ JSON + REDIRECT VỀ FRONTEND
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
                            String provider = token.getAuthorizedClientRegistrationId();
                            String code = request.getParameter("code"); // LẤY CODE TỪ URL

                            // Tạo URL redirect về frontend + truyền code
                            String redirectUrl = "http://localhost:5173/oauth/" + provider + "/callback?code=" + code;

                            response.sendRedirect(redirectUrl);
                        })
                        .failureHandler((request, response, exception) ->
                                sendJsonError(response, "Đăng nhập thất bại", HttpStatus.UNAUTHORIZED)
                        )
                )

                // JWT Filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Auth Provider
                .authenticationProvider(authenticationProvider())

                // Exception
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                );

        return http.build();
    }

    /**
     * XỬ LÝ OAuth2 THÀNH CÔNG:
     * 1. Tạo user + JWT token
     * 2. REDIRECT VỀ FRONTEND callback với query params
     */
    private void handleOAuth2Success(jakarta.servlet.http.HttpServletRequest request,
                                     HttpServletResponse response,
                                     org.springframework.security.core.Authentication authentication) throws IOException {
        try {
            if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
                sendJsonError(response, "Xác thực không hợp lệ", HttpStatus.BAD_REQUEST);
                return;
            }

            String provider = oauth2Token.getAuthorizedClientRegistrationId();
            OAuth2User oAuth2User = oauth2Token.getPrincipal();

            // Tạo user + token
            User user = userService.processOAuthUser(oAuth2User, provider);
            String token = jwtTokenProvider.generateToken(user.getEmail());

            // Tạo query params để frontend nhận
            String callbackUrl = String.format(
                    "http://localhost:5173/oauth/%s/callback?token=%s&user=%s",
                    provider.toLowerCase(),
                    token,
                    java.net.URLEncoder.encode(objectMapper.writeValueAsString(Map.of(
                            "id", user.getUserID(),
                            "email", user.getEmail(),
                            "name", user.getFullName(),
                            "role", user.getRoleId() != null ? user.getRoleId() : 3
                    )), "UTF-8")
            );

            // REDIRECT VỀ FRONTEND
            response.sendRedirect(callbackUrl);

        } catch (Exception e) {
            sendJsonError(response, "Lỗi xử lý OAuth: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Gửi JSON lỗi (nếu cần)
    private void sendJsonError(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> error = Map.of("success", false, "message", message);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    // CORS CONFIG
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:3000"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}