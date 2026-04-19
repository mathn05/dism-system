package com.csdlpt.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.csdlpt.web.entity.Station;
import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.security.AppUserPrincipal;
import com.csdlpt.web.service.InventoryService;
import com.csdlpt.web.service.InventoryService.AdjustmentResult;

@Controller
public class InventoryController {

    private static final int LOW_STOCK_THRESHOLD = 20;

    private final InventoryService inventoryService;
    private final StationRepository stationRepository;

    public InventoryController(InventoryService inventoryService, StationRepository stationRepository) {
        this.inventoryService = inventoryService;
        this.stationRepository = stationRepository;
    }

    @GetMapping("/inventory")
    public String inventoryPage(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        Station currentStation = loadCurrentStation(principal);
        String stationId = currentStation.getId();

        model.addAttribute("currentStation", currentStation);
        model.addAttribute("inventoryItems", inventoryService.getStationInventory(stationId));
        model.addAttribute("trackedProducts", inventoryService.getTrackedProductCount(stationId));
        model.addAttribute("lowStockCount", inventoryService.getLowStockCount(stationId, LOW_STOCK_THRESHOLD));
        model.addAttribute("totalUnits", inventoryService.getTotalQuantity(stationId));
        model.addAttribute("lowStockThreshold", LOW_STOCK_THRESHOLD);
        return "inventory";
    }

    @PostMapping("/inventory/adjust-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adjustStockAjax(@AuthenticationPrincipal AppUserPrincipal principal,
                                                               @RequestParam String productId,
                                                               @RequestParam int quantity,
                                                               @RequestParam String operation) {
        Map<String, Object> response = new HashMap<>();

        try {
            Station currentStation = loadCurrentStation(principal);
            int delta = mapAdjustmentDelta(quantity, operation);
            AdjustmentResult adjustmentResult = inventoryService.adjustQuantity(currentStation.getId(), productId, delta);

            response.put("success", true);
            response.put("message", "Stock updated successfully.");
            response.put("productId", adjustmentResult.productId());
            response.put("updatedQuantity", adjustmentResult.updatedQuantity());
            response.put("trackedProducts", inventoryService.getTrackedProductCount(currentStation.getId()));
            response.put("totalUnits", inventoryService.getTotalQuantity(currentStation.getId()));
            response.put("lowStockCount", inventoryService.getLowStockCount(currentStation.getId(), LOW_STOCK_THRESHOLD));
            response.put("lowStockThreshold", LOW_STOCK_THRESHOLD);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/inventory/receive")
    public String receiveStock(@AuthenticationPrincipal AppUserPrincipal principal,
                               @RequestParam String productId,
                               @RequestParam int quantity,
                               RedirectAttributes redirectAttributes) {
        try {
            inventoryService.receiveStock(loadCurrentStation(principal).getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Stock received successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/inventory";
    }

    @PostMapping("/inventory/delete-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteInventoryItem(@AuthenticationPrincipal AppUserPrincipal principal,
                                                                   @RequestParam String productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Station currentStation = loadCurrentStation(principal);
            inventoryService.deleteInventoryItem(currentStation.getId(), productId);

            response.put("success", true);
            response.put("message", "Product deleted from inventory.");
            response.put("productId", productId.trim());
            response.put("trackedProducts", inventoryService.getTrackedProductCount(currentStation.getId()));
            response.put("totalUnits", inventoryService.getTotalQuantity(currentStation.getId()));
            response.put("lowStockCount", inventoryService.getLowStockCount(currentStation.getId(), LOW_STOCK_THRESHOLD));
            response.put("lowStockThreshold", LOW_STOCK_THRESHOLD);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private Station loadCurrentStation(AppUserPrincipal principal) {
        if (principal == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return stationRepository.findById(principal.getStationId())
            .orElseThrow(() -> new IllegalStateException("Station not found for current user"));
    }

    private int mapAdjustmentDelta(int quantity, String operation) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("Adjustment operation is required");
        }

        return switch (operation.trim().toLowerCase()) {
            case "increase" -> quantity;
            case "decrease" -> -quantity;
            default -> throw new IllegalArgumentException("Unsupported adjustment operation");
        };
    }
}


