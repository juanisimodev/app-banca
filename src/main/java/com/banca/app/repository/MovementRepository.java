package com.banca.app.repository;

import com.banca.app.domain.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de movimientos y consultas de historial.
 */
@Repository
public interface MovementRepository extends JpaRepository<Movement, UUID> {
    
    @Query("SELECT m FROM Movement m WHERE m.cuenta.id IN :cuentaIds ORDER BY m.fecha DESC")
    List<Movement> findByCuentaIdInOrderByFechaDesc(@Param("cuentaIds") List<Long> cuentaIds);
}
