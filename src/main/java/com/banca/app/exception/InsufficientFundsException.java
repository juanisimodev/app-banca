package com.banca.app.exception;

/**
 * Excepcion lanzada cuando una cuenta no tiene saldo suficiente.
 */
public class InsufficientFundsException extends RuntimeException {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
}
