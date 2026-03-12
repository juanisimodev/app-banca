package com.banca.app.service;

import com.banca.app.domain.Account;
import com.banca.app.domain.AccountType;
import com.banca.app.domain.Role;
import com.banca.app.domain.User;
import com.banca.app.repository.AccountRepository;
import com.banca.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servicio administrativo para crear usuarios con cuentas iniciales.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final BigDecimal SALDO_INICIAL_AHORRO = new BigDecimal("50000.00");
    private static final BigDecimal SALDO_INICIAL_CORRIENTE = new BigDecimal("100000.00");

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUserWithDefaultAccounts(String rut, String nombreCompleto, String rawPassword, Role role) {
        String normalizedRut = rut == null ? "" : rut.trim();

        if (normalizedRut.isEmpty()) {
            throw new IllegalArgumentException("El RUT es obligatorio");
        }
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        if (userRepository.findByRut(normalizedRut).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese RUT");
        }

        User user = new User();
        user.setRut(normalizedRut);
        user.setNombreCompleto(nombreCompleto.trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        Account ahorro = new Account();
        ahorro.setNumeroCuenta(normalizedRut + "-A");
        ahorro.setTipo(AccountType.AHORRO);
        ahorro.setSaldo(SALDO_INICIAL_AHORRO);
        ahorro.setUsuario(savedUser);

        Account corriente = new Account();
        corriente.setNumeroCuenta(normalizedRut + "-C");
        corriente.setTipo(AccountType.CORRIENTE);
        corriente.setSaldo(SALDO_INICIAL_CORRIENTE);
        corriente.setUsuario(savedUser);

        accountRepository.save(ahorro);
        accountRepository.save(corriente);
    }
}
