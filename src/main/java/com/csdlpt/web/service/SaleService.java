package com.csdlpt.web.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.csdlpt.web.entity.AppUser;
import com.csdlpt.web.entity.Customer;
import com.csdlpt.web.entity.Product;
import com.csdlpt.web.entity.Sale;
import com.csdlpt.web.entity.SaleDetail;
import com.csdlpt.web.entity.SaleDetailId;
import com.csdlpt.web.entity.Station;
import com.csdlpt.web.repository.AppUserRepository;
import com.csdlpt.web.repository.CustomerRepository;
import com.csdlpt.web.repository.ProductRepository;
import com.csdlpt.web.repository.SaleDetailRepository;
import com.csdlpt.web.repository.SaleRepository;
import com.csdlpt.web.repository.StationRepository;

@Service
@Transactional
public class SaleService {

    public record SaleLineItem(String productId, int quantity, BigDecimal salePrice) {}

    public record SaleView(Sale sale, List<SaleDetail> details) {}

    private static final DateTimeFormatter ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final StationRepository stationRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public SaleService(SaleRepository saleRepository,
                       SaleDetailRepository saleDetailRepository,
                       AppUserRepository appUserRepository,
                       CustomerRepository customerRepository,
                       StationRepository stationRepository,
                       ProductRepository productRepository,
                       InventoryService inventoryService) {
        this.saleRepository = saleRepository;
        this.saleDetailRepository = saleDetailRepository;
        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.stationRepository = stationRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<SaleView> getStationSales(String stationId) {
        return saleRepository.findByStation_IdOrderByCreatedAtDescIdDesc(normalizeRequired(stationId, "Station is required"))
            .stream()
            .map(sale -> new SaleView(sale, saleDetailRepository.findBySale_IdOrderByProduct_IdAsc(sale.getId())))
            .toList();
    }

    public Sale createSale(String userId, String stationId, String customerId, List<SaleLineItem> lineItems) {
        String normalizedUserId = normalizeRequired(userId, "User is required");
        String normalizedStationId = normalizeRequired(stationId, "Station is required");
        String normalizedCustomerId = normalizeRequired(customerId, "Customer is required");
        List<SaleLineItem> normalizedItems = normalizeLineItems(lineItems);

        AppUser user = appUserRepository.findById(normalizedUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Station station = stationRepository.findById(normalizedStationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found"));
        Customer customer = customerRepository.findById(normalizedCustomerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        if (!station.getId().equalsIgnoreCase(user.getStation().getId())) {
            throw new IllegalArgumentException("User is not allowed to create sales for another station");
        }

        Sale sale = new Sale();
        sale.setId(generateId("SAL"));
        sale.setCreatedAt(LocalDateTime.now());
        sale.setUser(user);
        sale.setCustomer(customer);
        sale.setStation(station);
        Sale savedSale = saleRepository.save(sale);

        for (SaleLineItem lineItem : normalizedItems) {
            Product product = productRepository.findById(lineItem.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + lineItem.productId()));

            SaleDetail detail = new SaleDetail();
            detail.setId(new SaleDetailId(savedSale.getId(), product.getId()));
            detail.setSale(savedSale);
            detail.setProduct(product);
            detail.setQuantity(lineItem.quantity());
            detail.setSalePrice(lineItem.salePrice());
            saleDetailRepository.save(detail);

            inventoryService.adjustQuantity(station.getId(), product.getId(), -lineItem.quantity());
        }

        return savedSale;
    }

    private List<SaleLineItem> normalizeLineItems(List<SaleLineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("At least one sale item is required");
        }

        List<SaleLineItem> normalized = new ArrayList<>();
        Set<String> seenProductIds = new HashSet<>();
        for (SaleLineItem lineItem : lineItems) {
            if (lineItem == null) {
                continue;
            }

            String productId = normalizeRequired(lineItem.productId(), "Product ID is required");
            if (lineItem.quantity() <= 0) {
                throw new IllegalArgumentException("Sale quantity must be greater than zero");
            }
            if (lineItem.salePrice() == null || lineItem.salePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Sale price must be zero or greater");
            }
            if (!seenProductIds.add(productId)) {
                throw new IllegalArgumentException("Each product can only appear once in a sale receipt");
            }
            normalized.add(new SaleLineItem(productId, lineItem.quantity(), lineItem.salePrice()));
        }

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("At least one sale item is required");
        }
        return normalized;
    }

    private String generateId(String prefix) {
        return prefix + "-" + LocalDateTime.now().format(ID_FORMATTER) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
