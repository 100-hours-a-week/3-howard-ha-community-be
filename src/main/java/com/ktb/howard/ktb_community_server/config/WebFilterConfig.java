package com.ktb.howard.ktb_community_server.config;

import com.ktb.howard.ktb_community_server.auth.service.AuthService;
import com.ktb.howard.ktb_community_server.auth.service.JwtProvider;
import com.ktb.howard.ktb_community_server.filter.JwtAuthFilter;
import com.ktb.howard.ktb_community_server.filter.SessionAuthFilter;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class WebFilterConfig {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    /**
     * 세션기반 인증 Filter 등록
     */
    @Bean
    @ConditionalOnProperty(name = "app.auth.method", havingValue = "session")
    public FilterRegistrationBean<Filter> registerSessionAuthFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new SessionAuthFilter(authService));
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }

    /**
     * JWT 기반 인증 Filter 등록
     */
    @Bean
    @ConditionalOnProperty(name = "app.auth.method", havingValue = "jwt")
    public FilterRegistrationBean<Filter> registerJwtAuthFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new JwtAuthFilter(jwtProvider));
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }

}
