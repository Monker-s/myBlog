package com.monker.myblog.config;

import com.monker.myblog.dto.UserDto;
import com.monker.myblog.util.JwtUtil;
import com.monker.myblog.util.UserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 文件用途：JWT 认证过滤器。
 * 作用说明：拦截每个请求，从 Authorization 头中提取并验证 JWT Token，设置安全上下文。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            if (jwtUtil.isTokenValid(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                var claims = jwtUtil.parseToken(token);
                Integer role = claims.get("role", Integer.class);
                String username = claims.get("username", String.class);

                var authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );

                var authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 将用户信息存入 UserHolder，供 Service 层使用
                UserDto userDto = new UserDto();
                userDto.setId(userId);
                userDto.setUsername(username);
                userDto.setRole(role);
                UserHolder.setUserId(userDto);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清除 UserHolder，防止内存泄漏
            UserHolder.removeUserId();
        }
    }
}
