package com.banca.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de monitoreo de movimientos recientes para el rol GERENTE.
 */
@Getter
@AllArgsConstructor
public class ManagerMovementSummaryDTO {
    private LocalDateTime fecha;
    private String clienteRut;
    private String clienteNombre;
    private String numeroCuenta;
    private String descripcion;
    private BigDecimal monto;
    private boolean riesgoAlto;
}
