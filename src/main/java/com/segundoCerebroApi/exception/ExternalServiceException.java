package com.segundoCerebroApi.exception;

/** Falha ao comunicar com um serviço externo (ex.: provedor de textos bíblicos). */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
