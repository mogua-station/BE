package com.fesi6.team1.study_group.global.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieUtil jwtCookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = getJwtFromRequest(request);
        String refreshToken = getRefreshTokenFromRequest(request);  // 🔹 Refresh Token 가져오기

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
                if (refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken)) {
                    // 🔹 Refresh Token이 유효하면 새로운 Access Token 발급
                    Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
                    String newAccessToken = jwtTokenProvider.createAccessToken(userId);

                    // 🔹 새 Access Token을 쿠키에 저장
                    ResponseCookie newAccessTokenCookie = jwtCookieUtil.createAccessTokenCookie(newAccessToken);
                    response.addHeader("Set-Cookie", newAccessTokenCookie.toString());

                    // SecurityContext에 인증 정보 설정
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    filterChain.doFilter(request, response);
                    return;
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\": \"error\", \"data\": {}, \"message\": \"Access token has expired. Please login again.\"}");
                    return;
                }
            } catch (JwtException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
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
            return bearerToken.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
