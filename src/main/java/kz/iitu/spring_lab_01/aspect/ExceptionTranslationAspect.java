package kz.iitu.spring_lab_01.aspect;

import kz.iitu.spring_lab_01.exception.CatalogDomainException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(0) // Запускается первым, чтобы перехватывать и транслировать ошибки
public class ExceptionTranslationAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionTranslationAspect.class);

    @Around("kz.iitu.spring_lab_01.aspect.Pointcuts.serviceOperation()")
    public Object translateExceptions(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (IllegalArgumentException ex) {
            log.error("[EXCEPTION TRANSLATOR] Caught IllegalArgumentException: '{}'. Translating to CatalogDomainException...", ex.getMessage());
            throw new CatalogDomainException("Domain error: " + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            log.error("[EXCEPTION TRANSLATOR] Caught RuntimeException: '{}'. Translating to CatalogDomainException...", ex.getMessage());
            throw new CatalogDomainException("Unexpected domain failure: " + ex.getMessage(), ex);
        }
    }
}