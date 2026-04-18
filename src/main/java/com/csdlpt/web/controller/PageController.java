package com.csdlpt.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.csdlpt.web.entity.Station;
import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.security.AppUserPrincipal;

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
    public String dashboardPage(Authentication authentication, Model model) {
        addCurrentStation(authentication, model);
        return "dashboard";
    }


    @GetMapping("/masterdata")
    public String masterDataPage(Authentication authentication, Model model) {
        addCurrentStation(authentication, model);
        return "masterdata";
    }

    @GetMapping("/possale")
    public String posSalePage(Authentication authentication, Model model) {
        addCurrentStation(authentication, model);
        return "possale";
    }

    @GetMapping("/receive")
    public String receivePage(Authentication authentication, Model model) {
        addCurrentStation(authentication, model);
        return "receive";
    }

    private void addCurrentStation(Authentication authentication, Model model) {
        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Authenticated user is required");
        }

        if (!(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new IllegalStateException("Invalid authenticated user");
        }

        Station currentStation = stationRepository.findById(principal.getStationId())
            .orElseThrow(() -> new IllegalStateException("Station not found for current user"));

        model.addAttribute("currentStation", currentStation);
    }
}



