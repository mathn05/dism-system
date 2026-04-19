package com.csdlpt.web.service;

import com.csdlpt.web.entity.Customer;
import com.csdlpt.web.repository.CustomerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return customerRepository.count();
    }

    public Customer create(String customerId, String name, String phoneNumber, String address) {
        String normalizedId = normalizeCustomerId(customerId);
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

    public void delete(String customerId) {
        String normalizedId = normalizeCustomerId(customerId);

        Customer customer = customerRepository.findById(normalizedId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        try {
            customerRepository.delete(customer);
            customerRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Customer is being used in orders and cannot be deleted.");
        }
    }

    private String normalizeCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required.");
        }

        String normalized = customerId.trim();
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
