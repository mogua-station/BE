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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = getJwtFromRequest(request); // JWT 추출

        if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) { // accessToken 유효성 체크
            Long userId = jwtTokenProvider.getUserIdFromAccessToken(accessToken); // accessToken에서 userId 추출

            // CustomUserDetails로 인증 객체 생성
            CustomUserDetails userDetails = new CustomUserDetails(userId);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (accessToken != null && !jwtTokenProvider.validateAccessToken(accessToken)) {
            // accessToken이 만료된 경우, refreshToken 확인
            String refreshToken = request.getHeader("Refresh-Token");  // 헤더에서 refreshToken 추출

            if (refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken)) {
                Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken); // refreshToken에서 userId 추출

                // CustomUserDetails로 인증 객체 생성
                CustomUserDetails userDetails = new CustomUserDetails(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 새로운 accessToken 발급 후 response에 추가 (선택 사항)
                String newAccessToken = jwtTokenProvider.createAccessToken(userId);
                response.setHeader("Authorization", "Bearer " + newAccessToken);
            }
        }

        filterChain.doFilter(request, response); // 필터 체인 진행
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer "를 제외한 JWT 추출
        }
        return null;
    }
}
