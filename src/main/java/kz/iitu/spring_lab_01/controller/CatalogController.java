package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab4")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/item/{id}")
    public String getItem(@PathVariable long id) {
        return catalogService.findById(id);
    }

    @GetMapping("/items")
    public List<String> getItems(@RequestParam(defaultValue = "10") int limit) {
        return catalogService.findAll(limit);
    }

    @DeleteMapping("/item/{id}")
    public String deleteItem(@PathVariable long id) {
        return catalogService.remove(id);
    }

    @GetMapping("/proxy")
    public Map<String, Object> getProxyInfo() {
        return catalogService.getProxyInfo();
    }

    @GetMapping("/internal/direct")
    public List<String> internalDirect(@RequestParam(defaultValue = "3") int limit) {
        return catalogService.internalDirectCall(limit);
    }

    @GetMapping("/internal/aop-context")
    public List<String> internalAopContext(@RequestParam(defaultValue = "3") int limit) {
        return catalogService.internalViaAopContext(limit);
    }
}