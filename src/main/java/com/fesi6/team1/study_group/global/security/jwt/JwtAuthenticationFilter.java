package com.fesi6.team1.study_group.global.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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

        if (accessToken != null) {
            try {
                if (jwtTokenProvider.validateAccessToken(accessToken)) {
                    Long userId = jwtTokenProvider.getUserIdFromAccessToken(accessToken);

                    // Principal로 userId 설정
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    throw new ExpiredJwtException(null, null, "Access token has expired");
                }
            } catch (ExpiredJwtException e) {
                // 토큰 만료 시 401 Unauthorized로 처리하고 JSON 형식으로 메시지 전송
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");  // 응답 타입을 JSON으로 설정
                response.getWriter().write("{\"status\": \"error\", \"data\": {}, \"message\": \"Access token has expired. Please refresh your token.\"}");
                return;  // 더 이상 필터 체인을 진행하지 않음
            } catch (JwtException e) {
                // 다른 JwtException 처리를 추가할 수 있음
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");  // 응답 타입을 JSON으로 설정
                response.getWriter().write("{\"status\": \"error\", \"data\": {}, \"message\": \"Invalid token.\"}");
                return;
            }
        }
        response.setHeader("Partitioned", "true");
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