package kz.iitu.spring_lab_01.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(

        @NotBlank String owner,

        @NotBlank String group,

        @Valid Mail mail,

        @Valid Notification notification) {

    public record Mail(
            @NotBlank @Email String from,
            @Min(1) @Max(10) @DefaultValue("3") int retryCount,
            @DefaultValue("5s") Duration timeout,
            @DefaultValue("true") boolean enabled) {
    }

    // Индивидуальное задание: Вариант 10 (app.notification)
    public record Notification(
            @NotEmpty Map<String, String> channels,
            @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$") String quietHours) {
    }
}