package com.daily.note.save.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler successHandler;
    private final RateLimitInterceptor rateLimitInterceptor;

    SecurityConfig(JwtAuthFilter jwtAuthFilter, OAuth2SuccessHandler successHandler, RateLimitInterceptor rateLimitInterceptor) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.successHandler = successHandler;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/notes/**").authenticated()
                        .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(oauth -> oauth
                    .successHandler(successHandler)
                    .failureHandler((request, response, exception) -> {
                        response.sendRedirect("dailynote://login?error=true");
                    })
                )
                .addFilterBefore(rateLimitInterceptor, UsernamePasswordAuthenticationFilter.class) ✅
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
