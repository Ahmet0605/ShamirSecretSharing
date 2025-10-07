package com.example.shamir.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  // CSRF korumasını devre dışı bırakır
            .authorizeRequests()
                .anyRequest().permitAll()  // Tüm isteklere izin verir
            .and()
            .httpBasic().disable();  // HTTP Basic kimlik doğrulamayı devre dışı bırakır

        return http.build();
    }
}
