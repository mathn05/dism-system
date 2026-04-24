package com.csdlpt.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.csdlpt.web.entity.Station;
import com.csdlpt.web.repository.CustomerRepository;
import com.csdlpt.web.repository.ProductRepository;
import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.security.AppUserPrincipal;
import com.csdlpt.web.service.InventoryService;
import com.csdlpt.web.service.SaleService;

@Controller
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final StationRepository stationRepository;
    private final InventoryService inventoryService;

    public SaleController(SaleService saleService,
                          ProductRepository productRepository,
                          CustomerRepository customerRepository,
                          StationRepository stationRepository,
                          InventoryService inventoryService) {
        this.saleService = saleService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.stationRepository = stationRepository;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String salesPage(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        Station currentStation = loadCurrentStation(principal);
        model.addAttribute("currentStation", currentStation);
        model.addAttribute("products", productRepository.findAllByOrderByIdAsc());
        model.addAttribute("customers", customerRepository.findAllByOrderByIdAsc());
        model.addAttribute("sales", saleService.getStationSales(currentStation.getId()));
        model.addAttribute("inventoryItems", inventoryService.getStationInventory(currentStation.getId()));
        return "sales";
    }

    @PostMapping("/create")
    public String createSale(@AuthenticationPrincipal AppUserPrincipal principal,
                             @RequestParam String customerId,
                             @RequestParam("productId") List<String> productIds,
                             @RequestParam("quantity") List<Integer> quantities,
                             @RequestParam("salePrice") List<BigDecimal> salePrices,
                             RedirectAttributes redirectAttributes) {
        try {
            Station currentStation = loadCurrentStation(principal);
            saleService.createSale(principal.getId(), currentStation.getId(), customerId, buildLineItems(productIds, quantities, salePrices));
            redirectAttributes.addFlashAttribute("successMessage", "Sale receipt created successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/sales";
    }

    private List<SaleService.SaleLineItem> buildLineItems(List<String> productIds,
                                                          List<Integer> quantities,
                                                          List<BigDecimal> salePrices) {
        if (productIds == null || quantities == null || salePrices == null) {
            throw new IllegalArgumentException("Sale details are required.");
        }
        if (productIds.size() != quantities.size() || productIds.size() != salePrices.size()) {
            throw new IllegalArgumentException("Sale detail rows are invalid.");
        }

        List<SaleService.SaleLineItem> items = new ArrayList<>(productIds.size());
        for (int i = 0; i < productIds.size(); i++) {
            items.add(new SaleService.SaleLineItem(productIds.get(i), quantities.get(i), salePrices.get(i)));
        }
        return items;
    }

    private Station loadCurrentStation(AppUserPrincipal principal) {
        if (principal == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return stationRepository.findById(principal.getStationId())
            .orElseThrow(() -> new IllegalStateException("Station not found for current user"));
    }
}
