package com.csdlpt.web.repository;

import com.csdlpt.web.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    List<Customer> findAllByOrderByIdAsc();

    Optional<Customer> findByPhoneNumber(String phoneNumber);
}

