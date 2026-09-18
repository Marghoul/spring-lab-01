package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.lifecycle.LifecycleDemo;
import kz.iitu.spring_lab_01.notify.NotificationService;
import kz.iitu.spring_lab_01.notify.Notifier;
import kz.iitu.spring_lab_01.scope.TicketOffice;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {

    private final NotificationService notifications;
    private final LifecycleDemo lifecycle;
    private final TicketOffice ticketOffice;
    private final Notifier countingNotifier;

    public Lab2Controller(NotificationService notifications,
                          LifecycleDemo lifecycle,
                          TicketOffice ticketOffice,
                          @Qualifier("counting") Notifier countingNotifier) {
        this.notifications = notifications;
        this.lifecycle = lifecycle;
        this.ticketOffice = ticketOffice;
        this.countingNotifier = countingNotifier;
    }

    @GetMapping("/notify")
    public Map<String, Object> notify(@RequestParam(defaultValue = "Hello") String text) {
        return Map.of("primary",   notifications.viaPrimary(text),
                "console",   notifications.viaConsole(text),
                "all",       notifications.viaAll(text),
                "beanNames", notifications.names());
    }

    @GetMapping("/lifecycle")
    public List<String> lifecycle() {
        return lifecycle.events();
    }

    @GetMapping("/scopes")
    public Map<String, Object> scopes() {
        return ticketOffice.demo();
    }

    // Индивидуальное задание (Вариант 10)
    @GetMapping("/custom")
    public Map<String, String> custom(@RequestParam(defaultValue = "Hello World") String text) {
        return Map.of(
                "input", text,
                "result", countingNotifier.send(text)
        );
    }
}