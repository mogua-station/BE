package com.fesi6.team1.study_group.global.security;

import com.fesi6.team1.study_group.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] AUTH_WHITELIST = {
            "/user/kakao/**",// 카카오 로그인 콜백 URL
            "/user/sign-up",
            "/user/sign-in",
            "/meetups/list",
            "/home/health"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                });

        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers(AUTH_WHITELIST).permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 설정
        configuration.addAllowedOrigin("http://localhost:3000"); // 기존 로컬 개발 환경
        configuration.addAllowedOrigin("https://mogua-g109cgdv1-joshuayeyos-projects.vercel.app"); // 배포된 프론트엔드 도메인
        // 필요한 다른 도메인도 추가 가능

        // 허용할 HTTP 메서드 설정
        configuration.addAllowedMethod("*"); // 모든 메서드 허용

        // 허용할 헤더 설정
        configuration.addAllowedHeader("*"); // 모든 헤더 허용

        // 인증 정보 허용 여부 설정
        configuration.setAllowCredentials(true); // 쿠키 또는 인증 정보를 포함한 요청 허용

        // 클라이언트에서 접근할 수 있도록 노출할 헤더 설정
        configuration.addExposedHeader("Authorization"); // Authorization 헤더 노출

        // CORS 설정을 적용할 경로 매핑
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용

        return source;
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
