package com.csdlpt.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.csdlpt.web.entity.Station;
import com.csdlpt.web.entity.OrderType;
import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.security.AppUserPrincipal;
import com.csdlpt.web.service.InventoryService;
import com.csdlpt.web.service.OrderService;

@Controller
public class PageController {

    private final StationRepository stationRepository;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    public PageController(StationRepository stationRepository,
                          InventoryService inventoryService,
                          OrderService orderService) {
        this.stationRepository = stationRepository;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
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
        Station currentStation = (Station) model.getAttribute("currentStation");
        String stationId = currentStation.getId();

        model.addAttribute("trackedProducts", inventoryService.getTrackedProductCount(stationId));
        model.addAttribute("totalUnits", inventoryService.getTotalQuantity(stationId));
        model.addAttribute("lowStockCount", inventoryService.getLowStockCount(stationId, 20));
        model.addAttribute("totalOrders", orderService.countByStation(stationId));
        model.addAttribute("saleOrders", orderService.countByStationAndType(stationId, OrderType.SALE));
        model.addAttribute("transferOrders", orderService.countByStationAndType(stationId, OrderType.TRANSFER));
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
