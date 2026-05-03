package com.csdlpt.web.service;

import com.csdlpt.web.entity.Customer;
import com.csdlpt.web.repository.CustomerRepository;
import com.csdlpt.web.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    public record TopCustomerSummary(String customerName, long orderCount) {}

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final IdGenerationService idGenerationService;

    public CustomerService(CustomerRepository customerRepository,
                           SaleRepository saleRepository,
                           IdGenerationService idGenerationService) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.idGenerationService = idGenerationService;
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return customerRepository.count();
    }

    @Transactional(readOnly = true)
    public TopCustomerSummary findTopCustomerByStationId(String stationId) {
        return saleRepository.findTopCustomersByStationId(stationId).stream()
            .findFirst()
            .map(result -> new TopCustomerSummary(String.valueOf(result[0]), ((Number) result[1]).longValue()))
            .orElse(null);
    }

    public Customer create(String customerId, String name, String phoneNumber, String address) {
        String normalizedId = idGenerationService.nextCustomerId();
        String normalizedName = normalizeName(name);
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String normalizedAddress = normalizeAddress(address);

        if (customerRepository.existsById(normalizedId)) {
            throw new IllegalArgumentException("Customer ID already exists.");
        }
        ensurePhoneNumberUnique(normalizedPhone, null);

        Customer customer = new Customer();
        customer.setId(normalizedId);
        customer.setName(normalizedName);
        customer.setPhoneNumber(normalizedPhone);
        customer.setAddress(normalizedAddress);
        return customerRepository.save(customer);
    }

    public Customer update(String customerId, String name, String phoneNumber, String address) {
        String normalizedId = normalizeCustomerId(customerId);
        String normalizedName = normalizeName(name);
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String normalizedAddress = normalizeAddress(address);

        Customer customer = customerRepository.findById(normalizedId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        ensurePhoneNumberUnique(normalizedPhone, normalizedId);
        customer.setName(normalizedName);
        customer.setPhoneNumber(normalizedPhone);
        customer.setAddress(normalizedAddress);
        return customerRepository.save(customer);
    }

    private String normalizeCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required.");
        }

        String normalized = idGenerationService.normalizeManualId(customerId);
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Customer ID must not exceed 255 characters.");
        }
        return normalized;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }

        String normalized = name.trim().replaceAll("\\s{2,}", " ");
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Customer name must not exceed 255 characters.");
        }
        return normalized;
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        String normalized = phoneNumber.trim().replaceAll("\\s+", "");
        if (normalized.length() > 15) {
            throw new IllegalArgumentException("Phone number must not exceed 15 characters.");
        }
        return normalized;
    }

    private String normalizeAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address is required.");
        }

        String normalized = address.trim().replaceAll("\\s{2,}", " ");
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Address must not exceed 255 characters.");
        }
        return normalized;
    }

    private void ensurePhoneNumberUnique(String phoneNumber, String currentCustomerId) {
        customerRepository.findByPhoneNumber(phoneNumber).ifPresent(existing -> {
            if (currentCustomerId == null || !existing.getId().equals(currentCustomerId)) {
                throw new IllegalArgumentException("Phone number already exists.");
            }
        });
    }
}
