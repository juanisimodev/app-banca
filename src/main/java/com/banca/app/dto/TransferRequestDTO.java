package com.banca.app.dto;

import com.banca.app.domain.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO del formulario de transferencia entre cuentas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDTO {

    @NotBlank(message = "El RUT de destino es obligatorio")
    private String rutDestino;

    @NotNull(message = "El tipo de cuenta destino es obligatorio")
    private AccountType tipoCuentaDestino;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    private String descripcion;
}
