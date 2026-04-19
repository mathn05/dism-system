package com.csdlpt.web.controller;

import com.csdlpt.web.entity.AppUser;
import com.csdlpt.web.entity.Customer;
import com.csdlpt.web.entity.OrderEntity;
import com.csdlpt.web.entity.OrderType;
import com.csdlpt.web.entity.Station;
import com.csdlpt.web.repository.AppUserRepository;
import com.csdlpt.web.repository.CustomerRepository;
import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.security.AppUserPrincipal;
import com.csdlpt.web.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final StationRepository stationRepository;
    private final CustomerRepository customerRepository;
    private final AppUserRepository appUserRepository;

    public OrderController(OrderService orderService, StationRepository stationRepository, CustomerRepository customerRepository, AppUserRepository appUserRepository) {
        this.orderService = orderService;
        this.stationRepository = stationRepository;
        this.customerRepository = customerRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public String orderPage(Authentication authentication, Model model) {
        addCurrentStation(authentication, model);
        Station currentStation = (Station) model.getAttribute("currentStation");
        List<OrderEntity> stationOrders = orderService.findByStation(currentStation.getId());
        model.addAttribute("orders", stationOrders);
        model.addAttribute("saleOrders", stationOrders.stream()
            .filter(order -> order.getType() == OrderType.SALE)
            .toList());
        model.addAttribute("transferOrders", stationOrders.stream()
            .filter(order -> order.getType() == OrderType.TRANSFER)
            .toList());
        model.addAttribute("headquarterStations", stationRepository.findByHeadquarterTrueOrderByIdAsc());
        model.addAttribute("customers", customerRepository.findAllByOrderByIdAsc());
        return "order";
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @RequestParam String type,
                                         @RequestParam(required = false) String customerId,
                                         @RequestParam(required = false) String fromStationId,
                                         @RequestParam(required = false) String toStationId,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        try {
            OrderEntity order = new OrderEntity();
            order.setId(UUID.randomUUID().toString());

            OrderType orderType = OrderType.valueOf(type);
            order.setType(orderType);
            order.setCreatedAt(LocalDateTime.now());
            order.setUser(loadCurrentUser(principal));

            String currentStationId = principal.getStationId();
            if (orderType == OrderType.SALE) {
                if (customerId == null || customerId.isBlank()) {
                    throw new IllegalArgumentException("Customer is required for sale orders");
                }
                order.setCustomer(loadCustomer(customerId));
                order.setToStation(loadStation(currentStationId));
            }

            if (orderType == OrderType.TRANSFER) {
                Station headquarter = loadHeadquarterStation(fromStationId);
                order.setFromStation(headquarter);
                order.setToStation(loadStation(currentStationId));
            }

            OrderEntity savedOrder = orderService.save(order);
            redirectAttributes.addFlashAttribute("successMessage", "Order created successfully.");

            String requestType = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equalsIgnoreCase(requestType)) {
                return ResponseEntity.ok(savedOrder);
            }
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

            String requestType = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equalsIgnoreCase(requestType)) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/order").build();
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable String id, Authentication authentication, Model model) {
        addCurrentStation(authentication, model);
        Station currentStation = (Station) model.getAttribute("currentStation");
        OrderEntity order = orderService.findById(id);
        if (order == null || order.getToStation() == null || !currentStation.getId().equals(order.getToStation().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        model.addAttribute("order", order);
        return "order-detail";
    }

    private void addCurrentStation(Authentication authentication, Model model) {
        if (authentication == null
            || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new IllegalStateException("Authenticated user is required");
        }

        model.addAttribute("currentStation", loadStation(principal.getStationId()));
    }

    private AppUser loadCurrentUser(AppUserPrincipal principal) {
        if (principal == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return appUserRepository.findById(principal.getId())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private Station loadStation(String stationId) {
        return stationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));
    }

    private Customer loadCustomer(String customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
    }

    private Station loadHeadquarterStation(String stationId) {
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("Headquarter station is required for transfer orders");
        }

        Station station = loadStation(stationId);
        if (!station.isHeadquarter()) {
            throw new IllegalArgumentException("Transfer orders can only be received from the central warehouse");
        }
        return station;
    }
}
