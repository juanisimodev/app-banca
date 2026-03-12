package com.banca.app.exception;

/**
 * Excepcion de negocio para errores generales durante transferencias.
 */
public class TransferException extends RuntimeException {
    
    public TransferException(String message) {
        super(message);
    }
    
    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
