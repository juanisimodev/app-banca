package com.banca.app.exception;

/**
 * Excepcion lanzada cuando una cuenta no existe en la base de datos.
 */
public class AccountNotFoundException extends RuntimeException {
    
    public AccountNotFoundException(String message) {
        super(message);
    }
}
