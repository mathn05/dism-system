package com.csdlpt.web.controller;

import com.csdlpt.web.entity.Customer;
import com.csdlpt.web.entity.Station;
import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.security.AppUserPrincipal;
import com.csdlpt.web.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final StationRepository stationRepository;

    public CustomerController(CustomerService customerService,
                              StationRepository stationRepository) {
        this.customerService = customerService;
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public String customerPage(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        Station currentStation = loadCurrentStation(principal);
        model.addAttribute("currentStation", currentStation);
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("customerCount", customerService.countAll());
        return "customer";
    }

    @PostMapping("/create-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCustomer(@RequestParam String customerId,
                                                              @RequestParam String name,
                                                              @RequestParam String phoneNumber,
                                                              @RequestParam String address) {
        return handleCustomerResponse(
            () -> customerService.create(customerId, name, phoneNumber, address),
            "Customer created successfully."
        );
    }

    @PostMapping("/update-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCustomer(@RequestParam String customerId,
                                                              @RequestParam String name,
                                                              @RequestParam String phoneNumber,
                                                              @RequestParam String address) {
        return handleCustomerResponse(
            () -> customerService.update(customerId, name, phoneNumber, address),
            "Customer updated successfully."
        );
    }

    @PostMapping("/delete-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCustomer(@RequestParam String customerId) {
        Map<String, Object> response = new HashMap<>();
        try {
            customerService.delete(customerId);
            response.put("success", true);
            response.put("message", "Customer deleted successfully.");
            response.put("customerId", customerId.trim());
            response.put("customerCount", customerService.countAll());
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> handleCustomerResponse(CustomerSupplier supplier, String message) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = supplier.get();
            response.put("success", true);
            response.put("message", message);
            response.put("customer", customer);
            response.put("customerCount", customerService.countAll());
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

    @FunctionalInterface
    private interface CustomerSupplier {
        Customer get();
    }
}
