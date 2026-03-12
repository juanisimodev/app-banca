package com.banca.app.dto;

import com.banca.app.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO de resumen de usuarios para el panel de administracion.
 */
@Getter
@AllArgsConstructor
public class AdminUserSummaryDTO {
    private String rut;
    private String nombreCompleto;
    private Role role;
    private BigDecimal saldoAhorro;
    private BigDecimal saldoCorriente;
    private BigDecimal saldoTotal;
}
