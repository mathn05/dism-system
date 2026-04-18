package com.csdlpt.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.csdlpt.web.repository.StationRepository;

@Controller
public class PageController {

    private final StationRepository stationRepository;

    public PageController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication, Model model) {
        if (authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("stations", stationRepository.findAllByOrderByIdAsc());
        return "login";
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/inventory")
    public String inventoryPage() {
        return "inventory";
    }

    @GetMapping("/masterdata")
    public String masterDataPage() {
        return "masterdata";
    }

    @GetMapping("/possale")
    public String posSalePage() {
        return "possale";
    }

    @GetMapping("/receive")
    public String receivePage() {
        return "receive";
    }
}



