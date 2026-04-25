package com.csdlpt.web.security;

import com.csdlpt.web.service.AppUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String[] STATION_USER_PATHS = {
        "/",
        "/dashboard",
        "/inventory",
        "/inventory/**",
        "/imports",
        "/imports/**",
        "/sales",
        "/sales/**",
        "/customer",
        "/customer/**"
    };

    private static final String[] SHARED_ACCESS_PATHS = {
        "/masterdata",
        "/masterdata/**"
    };

    @Bean
    public StationAuthenticationProvider stationAuthenticationProvider(AppUserDetailsService appUserDetailsService,
                                                                       PasswordEncoder passwordEncoder) {
        return new StationAuthenticationProvider(appUserDetailsService, passwordEncoder);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   StationAuthenticationProvider stationAuthenticationProvider,
                                                   StationWebAuthenticationDetailsSource detailsSource) throws Exception {
        http
            .authenticationProvider(stationAuthenticationProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .requestMatchers(STATION_USER_PATHS).hasAnyRole("ADMIN", "BRANCH_STAFF")
                .requestMatchers(SHARED_ACCESS_PATHS).hasAnyRole("ADMIN", "BRANCH_STAFF")
                .anyRequest().denyAll())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .authenticationDetailsSource(detailsSource)
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .rememberMe(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword == null ? null : rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword != null && rawPassword.toString().equals(encodedPassword);
            }
        };
    }
}

