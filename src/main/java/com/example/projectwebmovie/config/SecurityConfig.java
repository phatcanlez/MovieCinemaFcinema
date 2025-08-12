package com.example.projectwebmovie.config;

import com.example.projectwebmovie.repository.AccountRepository;
import com.example.projectwebmovie.config.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(AccountRepository accountRepository) {
        return username -> accountRepository.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Tên đăng nhập hoặc mật khẩu không đúng."));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            System.out.println(authentication);
            if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                response.sendRedirect("/admin/dashboard");
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_EMPLOYEE"))) {
                response.sendRedirect("/employee/ticket-management");
            } else {
                response.sendRedirect("/home");
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler successGoogleHandler() {
        return (request, response, authentication) -> {
            System.out.println(authentication);
            response.sendRedirect("/auth/login"); // Redirect to home or a specific OAuth2 success page if needed
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/home/**", "/homeLayout/**", "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/auth/login", "/auth/register", "/auth/forgot-password",
                                "/auth/verify-otp", "/auth/reset-password", "/delete-account/**", "/error",
                                "/privacy-policy", "/about-theater", "/utility", "/contact", "/partner",
                                "/chat-recommendation", "/movie-list", "/movie-detail/**", "/recruitment",
                                "/movies/search", "/faq", "guide", "/api/**")

                        .permitAll()
                        // THÊM CÁC ĐƯỜNG DẪN SWAGGER VÀO DANH SÁCH PERMITALL
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api/**").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/home/booking/**").hasAnyRole("USER", "EMPLOYEE", "ADMIN")
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .successHandler(successGoogleHandler())
                        .failureUrl("/auth/login?error=true"))
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/auth/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .permitAll())
                .csrf(csrf -> csrf.disable()); // Đề xuất giữ CSRF nếu có thể, nhưng với REST API có thể tắt

        return http.build();
    }
}