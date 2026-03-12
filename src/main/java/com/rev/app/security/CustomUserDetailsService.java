package com.rev.app.security;

import com.rev.app.entity.User;
import com.rev.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest request;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        String normalizedEmail = email.toLowerCase().trim();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + normalizedEmail));

        String portalRole = request.getParameter("portalRole");

        if (portalRole != null && !portalRole.isBlank()) {

            String userRole = user.getRole().name();

            if (!userRole.equalsIgnoreCase(portalRole)) {
                throw new UsernameNotFoundException("Access denied for this portal");
            }
        }

        // Always enable authentication so password gets validated first
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                true,
                true,
                true,
                true,
                Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole().name())
                )
        );
    }
}