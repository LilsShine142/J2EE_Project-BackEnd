package com.example.j2ee_project.security;

import com.example.j2ee_project.utils.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    // Sử dụng constructor injection thay vì @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // For optional auth endpoints, if no token, proceed without authentication
        String jwt = getJwtFromRequest(request);
        if (jwt == null && isOptionalAuthEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwt != null && jwtTokenProvider.verifyToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Authenticated user: {}", username);
                }
            }
        } catch (Exception ex) {
            // Không xử lý lỗi ở đây - để AuthenticationEntryPoint xử lý
            logger.error("Failed to authenticate user from JWT", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Optional: Bỏ qua filter cho một số endpoint
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Bỏ qua filter cho các endpoint public
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/public/")
                || path.equals("/api/users/register");
    }

    private boolean isOptionalAuthEndpoint(String path) {
        // Định nghĩa các endpoint không bắt buộc phải có token ở đây
        return path.equals("/api/categories/getall") ||
               path.equals("/api/meals/popular") ||
               path.startsWith("/api/meals/category/") ||
               path.equals("/api/tabletypes/getall") ||
               path.equals("/api/meals/getall");
    }

}