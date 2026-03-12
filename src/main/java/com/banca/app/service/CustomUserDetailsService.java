package com.banca.app.service;

import com.banca.app.domain.User;
import com.banca.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Adapta el usuario de la base de datos al modelo UserDetails de Spring Security.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String rut) throws UsernameNotFoundException {
        // Buscar usuario por RUT en la base de datos
        User user = userRepository.findByRut(rut)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con RUT: " + rut));

        // Retornar UserDetails con RUT como username
        return new org.springframework.security.core.userdetails.User(
                user.getRut(), // Username es el RUT
                user.getPassword(),
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user)
        );
    }

    /**
     * Convierte el Role del usuario en GrantedAuthority para Spring Security
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
}
