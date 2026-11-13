package kz.iitu.spring_lab_01.bank;

/** Checked exception: by default Spring does NOT roll back a transaction for it. */
public class BusinessException extends Exception {
    public BusinessException(String message) {
        super(message);
    }
}
