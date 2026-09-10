package kz.iitu.spring_lab_01.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${app.owner:unknown}")
    private String owner;

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(defaultValue = "world") String name) {
        return new Greeting("Hello, " + name + "!", owner, LocalDateTime.now());
    }

    @GetMapping("/info")
    public Info info() {
        return new Info(owner,
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors());
    }

    @GetMapping("/calc")
    public CalcResult calculate(@RequestParam(defaultValue = "0") double a,
                                @RequestParam(defaultValue = "0") double b,
                                @RequestParam(defaultValue = "add") String op) {
        double result = switch (op) {
            case "sub" -> a - b;
            case "mul" -> a * b;
            case "div" -> b != 0 ? a / b : 0;
            default -> a + b;
        };
        return new CalcResult(a, b, op, result);
    }

    public record Greeting(String message, String owner, LocalDateTime timestamp) { }
    public record Info(String owner, String javaVersion, int cpuCores) { }
    public record CalcResult(double a, double b, String operation, double result) { }
}