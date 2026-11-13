package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.bank.Account;
import kz.iitu.spring_lab_01.bank.AccountRepository;
import kz.iitu.spring_lab_01.bank.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository repository;
    private final TransferService transferService;

    public AccountController(AccountRepository repository, TransferService transferService) {
        this.repository = repository;
        this.transferService = transferService;
    }

    @GetMapping
    public List<Account> all() {
        return repository.findAll();
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestParam Long from, @RequestParam Long to,
                                      @RequestParam BigDecimal amount) {
        try {
            transferService.transfer(from, to, amount);
            return ResponseEntity.ok(repository.findAll());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage(), "accounts", repository.findAll()));
        }
    }
}
