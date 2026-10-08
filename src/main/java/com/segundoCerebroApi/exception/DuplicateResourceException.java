package com.segundoCerebroApi.exception;

/**
 * Lançada quando a requisição viola uma restrição de unicidade
 * (e-mail ou CPF já cadastrados). Mapeada para HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
