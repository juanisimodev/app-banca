package com.banca.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de supervision de clientes para el dashboard de GERENTE.
 */
@Getter
@AllArgsConstructor
public class ManagerClientSummaryDTO {
    private String rut;
    private String nombreCompleto;
    private BigDecimal saldoAhorro;
    private BigDecimal saldoCorriente;
    private BigDecimal saldoTotal;
    private LocalDateTime ultimoMovimiento;
}
