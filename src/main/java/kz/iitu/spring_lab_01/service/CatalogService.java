package kz.iitu.spring_lab_01.service;

import kz.iitu.spring_lab_01.audit.Audited;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class CatalogService {

    public String findById(long id) {
        sleep(50);
        return "Item no. " + id;
    }

    @Audited(action = "CATALOG_LIST", logArguments = true)
    public List<String> findAll(int limit) {
        sleep(300);
        return IntStream.rangeClosed(1, limit)
                .mapToObj(i -> "Item no. " + i)
                .toList();
    }

    @Audited(action = "CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
        return "Removed item no. " + id;
    }

    // 1. Прямой внутренний вызов (аспект НЕ сработает)
    public List<String> internalDirectCall(int limit) {
        return this.findAll(limit);
    }

    // 2. Внутренний вызов через AopContext (аспект СРАБОТАЕТ)
    public List<String> internalViaAopContext(int limit) {
        return ((CatalogService) AopContext.currentProxy()).findAll(limit);
    }

    // Информация о прокси-классе
    public Map<String, Object> getProxyInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("className", this.getClass().getName());
        info.put("isCglibProxy", this.getClass().getName().contains("$$SpringCGLIB$$"));
        info.put("superclass", this.getClass().getSuperclass().getName());
        return info;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}