package io.github.tony8864.config;

import io.github.tony8864.jwt.JwtAuthenticationFilter;
import io.github.tony8864.jwt.JwtTokenService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    private final JwtTokenService tokenService;

    public WebConfig(JwtTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter(tokenService));
        registration.addUrlPatterns("/api/*"); // apply to all API endpoints
        registration.setOrder(1); // ensure it runs early
        return registration;
    }
}
