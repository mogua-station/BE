package com.fesi6.team1.study_group.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = getJwtFromRequest(request);

        if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) {
            Long userId = jwtTokenProvider.getUserIdFromAccessToken(accessToken);

            // Principal로 userId 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (accessToken != null && !jwtTokenProvider.validateAccessToken(accessToken)) {
            String refreshToken = request.getHeader("Refresh-Token");

            if (refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken)) {
                Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 새로운 accessToken 발급 후 response에 추가 (선택 사항)
                String newAccessToken = jwtTokenProvider.createAccessToken(userId);
                response.setHeader("Authorization", "Bearer " + newAccessToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer "를 제외한 JWT 추출
        }
        return null;
    }
}
