package com.banca.app.repository;

import com.banca.app.domain.Account;
import com.banca.app.domain.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de cuentas bancarias.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByUsuarioRutAndTipo(String rut, AccountType type);
}
