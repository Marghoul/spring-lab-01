package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.security.AppUserRepository;
import kz.iitu.spring_lab_01.security.AuthAuditRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AppUserRepository users;
    private final AuthAuditRepository audit;

    public AdminController(AppUserRepository users, AuthAuditRepository audit) {
        this.users = users;
        this.audit = audit;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String index(Model model) {
        model.addAttribute("users", users.findAll(Sort.by("id")));
        model.addAttribute("audit", audit.findTop20ByOrderByIdDesc());
        return "admin/index";
    }
}
