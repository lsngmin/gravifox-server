package com.gravifox.security.config;

import com.gravifox.security.jwt.filter.JWTCheckFilter;
import com.gravifox.security.handler.RestAccessDeniedHandler;
import com.gravifox.security.handler.RestAuthenticationEntryPoint;
import com.gravifox.security.path.RequestPathMatcher;
import com.gravifox.domain.member.service.oauth2.OAuth2UserService;
import com.gravifox.domain.member.service.oauth2.OAuth2UserSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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


import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private JWTCheckFilter jwtCheckFilter;
    private final OAuth2UserService OAuth2UserService;
    private final OAuth2UserSuccessHandler oAuth2UserSuccessHandler;
    private final RestAuthenticationEntryPoint authenticationEntryPoint = new RestAuthenticationEntryPoint();
    private final RestAccessDeniedHandler accessDeniedHandler = new RestAccessDeniedHandler();

    @Value("${front.redirect.login-url:/login}") private String loginUrl;
    @Value("${cors.allowed-origins:*}") private String allowedOrigins;

    @Autowired
    private void setJwtCheckFilter(JWTCheckFilter jwtCheckFilter) {this.jwtCheckFilter = jwtCheckFilter;}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(RequestPathMatcher.PUBLIC_PATTERNS.toArray(new String[0])).permitAll()
                        .anyRequest().authenticated()
                )
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/v3/api-docs/**",
//                                "/swagger-ui/**",
//                                "/swagger-ui.html",
//                                "/docs/**",
//                                "/api/upload-swagger",
//                                "/health/**",
//                                "/api/v1/register/**",
//                                "/api/v1/auth/login",
//                                "/api/v1/auth/refresh",
//                                "/api/v1/auth/email/**"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )

                .addFilterBefore(jwtCheckFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .oauth2Login(oauth -> oauth
                        .loginPage(loginUrl)
                        .userInfoEndpoint(userInfo -> userInfo.userService(OAuth2UserService))
                        .successHandler(oAuth2UserSuccessHandler)
                );

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // Configure allowed origins from property. If "*", use patterns; otherwise explicit origins.
        if (allowedOrigins != null && (allowedOrigins.equals("*") || allowedOrigins.contains("*"))) {
            corsConfiguration.setAllowedOriginPatterns(List.of("*"));
        } else if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            List<String> origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            corsConfiguration.setAllowedOrigins(origins);
        } else {
            corsConfiguration.setAllowedOriginPatterns(List.of("*"));
        }
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "HEAD", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of(
                "Authorization", "Cache-Control", "Content-Type", "X-Request-ID",
                "If-None-Match", "If-Match", "Upload-Token", "X-Service-Key"
        ));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
