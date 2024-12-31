package com.fesi6.team1.study_group.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = getJwtFromRequest(request); // 요청에서 JWT 추출
        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {  // JWT가 유효한지 확인
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);  // JWT에서 사용자 ID 추출

            // JWT가 유효하다면 인증된 사용자 객체 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);  // 인증 정보 설정
        }

        filterChain.doFilter(request, response);  // 필터 체인 진행
    }

    // HTTP 요청 헤더에서 JWT 추출
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer "를 제외한 실제 JWT 토큰 추출
        }
        return null;
    }
}
