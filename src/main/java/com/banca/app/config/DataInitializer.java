package com.banca.app.config;

import com.banca.app.domain.*;
import com.banca.app.repository.AccountRepository;
import com.banca.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * Carga datos base de usuarios y cuentas al iniciar la aplicacion.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Iniciando carga de datos de prueba...");

            // Verificar y crear usuario SYS_ADMIN
            if (userRepository.findByRut("1-9").isEmpty()) {
                log.info("Creando usuario SYS_ADMIN...");
                
                User admin = new User();
                admin.setRut("1-9");
                admin.setNombreCompleto("Administrador del Sistema");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.SYS_ADMIN);
                
                userRepository.save(admin);
                log.info("Usuario SYS_ADMIN creado exitosamente (RUT: 1-9)");
            } else {
                log.info("Usuario SYS_ADMIN ya existe (RUT: 1-9)");
            }

            // Verificar y crear usuario de prueba
            if (userRepository.findByRut("12.345.678-9").isEmpty()) {
                log.info("Creando usuario de prueba...");
                
                User usuario = new User();
                usuario.setRut("12.345.678-9");
                usuario.setNombreCompleto("Juan Pérez González");
                usuario.setPassword(passwordEncoder.encode("user123"));
                usuario.setRole(Role.USER);
                
                userRepository.save(usuario);
                log.info("Usuario de prueba creado exitosamente (RUT: 12.345.678-9)");

                // Crear cuenta de AHORRO
                Account cuentaAhorro = new Account();
                cuentaAhorro.setNumeroCuenta("12.345.678-9-A");
                cuentaAhorro.setTipo(AccountType.AHORRO);
                cuentaAhorro.setSaldo(new BigDecimal("50000"));
                cuentaAhorro.setUsuario(usuario);
                
                accountRepository.save(cuentaAhorro);
                log.info("Cuenta de Ahorro creada con saldo: $50,000");

                // Crear cuenta CORRIENTE
                Account cuentaCorriente = new Account();
                cuentaCorriente.setNumeroCuenta("12.345.678-9-C");
                cuentaCorriente.setTipo(AccountType.CORRIENTE);
                cuentaCorriente.setSaldo(new BigDecimal("100000"));
                cuentaCorriente.setUsuario(usuario);
                
                accountRepository.save(cuentaCorriente);
                log.info("Cuenta Corriente creada con saldo: $100,000");
            } else {
                log.info("Usuario de prueba ya existe (RUT: 12.345.678-9)");
            }

            log.info("Carga de datos de prueba completada.");
        };
    }
}
