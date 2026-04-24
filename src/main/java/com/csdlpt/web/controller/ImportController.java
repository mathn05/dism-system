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
import com.csdlpt.web.repository.ProductRepository;
import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.repository.SupplierRepository;
import com.csdlpt.web.security.AppUserPrincipal;
import com.csdlpt.web.service.ImportService;

@Controller
@RequestMapping("/imports")
public class ImportController {

    private final ImportService importService;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final StationRepository stationRepository;

    public ImportController(ImportService importService,
                            ProductRepository productRepository,
                            SupplierRepository supplierRepository,
                            StationRepository stationRepository) {
        this.importService = importService;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public String importsPage(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        Station currentStation = loadCurrentStation(principal);
        model.addAttribute("currentStation", currentStation);
        model.addAttribute("products", productRepository.findAllByOrderByIdAsc());
        model.addAttribute("suppliers", supplierRepository.findAllByOrderByIdAsc());
        model.addAttribute("imports", importService.getStationImports(currentStation.getId()));
        return "imports";
    }

    @PostMapping("/create")
    public String createImport(@AuthenticationPrincipal AppUserPrincipal principal,
                               @RequestParam String supplierId,
                               @RequestParam("productId") List<String> productIds,
                               @RequestParam("quantity") List<Integer> quantities,
                               @RequestParam("importPrice") List<BigDecimal> importPrices,
                               RedirectAttributes redirectAttributes) {
        try {
            Station currentStation = loadCurrentStation(principal);
            importService.createImport(principal.getId(), currentStation.getId(), supplierId, buildLineItems(productIds, quantities, importPrices));
            redirectAttributes.addFlashAttribute("successMessage", "Import receipt created successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/imports";
    }

    private List<ImportService.ImportLineItem> buildLineItems(List<String> productIds,
                                                              List<Integer> quantities,
                                                              List<BigDecimal> importPrices) {
        if (productIds == null || quantities == null || importPrices == null) {
            throw new IllegalArgumentException("Import details are required.");
        }
        if (productIds.size() != quantities.size() || productIds.size() != importPrices.size()) {
            throw new IllegalArgumentException("Import detail rows are invalid.");
        }

        List<ImportService.ImportLineItem> items = new ArrayList<>(productIds.size());
        for (int i = 0; i < productIds.size(); i++) {
            items.add(new ImportService.ImportLineItem(productIds.get(i), quantities.get(i), importPrices.get(i)));
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
