package com.rev.app.config;

import com.rev.app.repository.UserRepository;
import com.rev.app.security.JwtAccessDeniedHandler;
import com.rev.app.security.JwtAuthenticationEntryPoint;
import com.rev.app.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    private UserRepository userRepository;

    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();

    }

    // ---------------- API SECURITY ----------------
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/register").hasRole("ADMIN")
                    .requestMatchers("/api/employees/**").hasAnyRole("ADMIN","MANAGER","EMPLOYEE")
                    .requestMatchers("/api/leaves/**").hasAnyRole("ADMIN","MANAGER","EMPLOYEE")
                    .requestMatchers("/api/notifications/**").hasAnyRole("ADMIN","MANAGER","EMPLOYEE")
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ---------------- WEB SECURITY ----------------
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/",
                            "/register",
                            "/register/**",
                            "/login",
                            "/login/**",
                            "/css/**",
                            "/js/**",
                            "/static/**",
                            "/h2-console/**"
                    ).permitAll()

                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/manager/**").hasRole("MANAGER")

                    .anyRequest().authenticated()
            )

            .formLogin(form -> form

                    .loginPage("/login")

                    // SUCCESS LOGIN
                    .successHandler((request, response, authentication) -> {

                        String email = authentication.getName();
                        String portalRole = request.getParameter("portalRole");

                        var userOpt = userRepository.findByEmailIgnoreCase(email);

                        if (userOpt.isPresent()) {

                            int status = userOpt.get().getIsActive();

                            if ("ROLE_EMPLOYEE".equals(portalRole)) {

                                if (status == 0) {
                                    response.sendRedirect("/login/employee?notapproved");
                                    return;
                                }

                                if (status == 2) {
                                    response.sendRedirect("/login/employee?deactivated");
                                    return;
                                }
                            }

                            if ("ROLE_MANAGER".equals(portalRole)) {

                                if (status == 0) {
                                    response.sendRedirect("/login/manager?notapproved");
                                    return;
                                }
                            }
                        }

                        response.sendRedirect("/dashboard");

                    })

                    // LOGIN FAILURE
                    .failureHandler((request, response, exception) -> {

                        String portalRole = request.getParameter("portalRole");

                        if ("ROLE_EMPLOYEE".equals(portalRole)) {

                            response.sendRedirect("/login/employee?error");

                        } else if ("ROLE_MANAGER".equals(portalRole)) {

                            response.sendRedirect("/login/manager?error");

                        } else if ("ROLE_ADMIN".equals(portalRole)) {

                            response.sendRedirect("/login/admin?error");

                        } else {

                            response.sendRedirect("/login?error");
                        }

                    })

                    .permitAll()
            )

            .logout(logout -> logout
                    .logoutSuccessUrl("/")
                    .permitAll()
            );

        return http.build();
    }
}