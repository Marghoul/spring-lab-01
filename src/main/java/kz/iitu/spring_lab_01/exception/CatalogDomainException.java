package kz.iitu.spring_lab_01.exception;

public class CatalogDomainException extends RuntimeException {
    public CatalogDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}