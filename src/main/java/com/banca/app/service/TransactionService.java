package com.banca.app.service;

import com.banca.app.domain.Account;
import com.banca.app.domain.AccountType;
import com.banca.app.domain.Movement;
import com.banca.app.exception.AccountNotFoundException;
import com.banca.app.exception.InsufficientFundsException;
import com.banca.app.exception.TransferException;
import com.banca.app.repository.AccountRepository;
import com.banca.app.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio de negocio para ejecutar transferencias bancarias de forma atomica.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;

    /**
     * Realiza una transferencia bancaria entre dos cuentas de forma atómica.
     * 
     * @param rutOrigen RUT del usuario origen
     * @param tipoCuentaOrigen Tipo de cuenta origen (AHORRO/CORRIENTE)
     * @param rutDestino RUT del usuario destino
     * @param tipoCuentaDestino Tipo de cuenta destino (AHORRO/CORRIENTE)
     * @param monto Monto a transferir
     * @throws AccountNotFoundException Si alguna de las cuentas no existe
     * @throws InsufficientFundsException Si la cuenta origen no tiene saldo suficiente
     * @throws TransferException Si ocurre algún error durante la transferencia
     */
    @Transactional
    public void realizarTransferencia(
            String rutOrigen,
            AccountType tipoCuentaOrigen,
            String rutDestino,
            AccountType tipoCuentaDestino,
            BigDecimal monto) {

        // Validar que el monto sea positivo
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferException("El monto debe ser mayor a cero");
        }

        // 1. Buscar la cuenta origen
        Account cuentaOrigen = accountRepository
                .findByUsuarioRutAndTipo(rutOrigen, tipoCuentaOrigen)
                .orElseThrow(() -> new AccountNotFoundException(
                        String.format("Cuenta origen no encontrada (RUT: %s, Tipo: %s)", 
                                rutOrigen, tipoCuentaOrigen)));

        // 2. Validar que tenga saldo suficiente
        if (cuentaOrigen.getSaldo().compareTo(monto) < 0) {
            throw new InsufficientFundsException(
                    String.format("Saldo insuficiente. Saldo actual: $%s, Monto solicitado: $%s",
                            cuentaOrigen.getSaldo(), monto));
        }

        // 3. Buscar la cuenta destino por RUT y Tipo
        Account cuentaDestino = accountRepository
                .findByUsuarioRutAndTipo(rutDestino, tipoCuentaDestino)
                .orElseThrow(() -> new AccountNotFoundException(
                        String.format("Cuenta destino no encontrada (RUT: %s, Tipo: %s)", 
                                rutDestino, tipoCuentaDestino)));

        // 4. Restar al origen
        BigDecimal nuevoSaldoOrigen = cuentaOrigen.getSaldo().subtract(monto);
        cuentaOrigen.setSaldo(nuevoSaldoOrigen);

        // 5. Sumar al destino
        BigDecimal nuevoSaldoDestino = cuentaDestino.getSaldo().add(monto);
        cuentaDestino.setSaldo(nuevoSaldoDestino);

        // Guardar las cuentas actualizadas
        accountRepository.save(cuentaOrigen);
        accountRepository.save(cuentaDestino);

        // 6. Crear registro de movimiento para el origen (cargo - negativo)
        Movement movimientoOrigen = new Movement();
        movimientoOrigen.setFecha(LocalDateTime.now());
        movimientoOrigen.setDescripcion(
                String.format("Transferencia a %s - Cuenta %s", 
                        cuentaDestino.getUsuario().getNombreCompleto(),
                        cuentaDestino.getNumeroCuenta()));
        movimientoOrigen.setMonto(monto.negate()); // Negativo para cargo
        movimientoOrigen.setSaldoResultante(nuevoSaldoOrigen);
        movimientoOrigen.setCuenta(cuentaOrigen);

        // 7. Crear registro de movimiento para el destino (abono - positivo)
        Movement movimientoDestino = new Movement();
        movimientoDestino.setFecha(LocalDateTime.now());
        movimientoDestino.setDescripcion(
                String.format("Transferencia de %s - Cuenta %s",
                        cuentaOrigen.getUsuario().getNombreCompleto(),
                        cuentaOrigen.getNumeroCuenta()));
        movimientoDestino.setMonto(monto); // Positivo para abono
        movimientoDestino.setSaldoResultante(nuevoSaldoDestino);
        movimientoDestino.setCuenta(cuentaDestino);

        // Guardar los dos movimientos
        movementRepository.save(movimientoOrigen);
        movementRepository.save(movimientoDestino);
    }
}
