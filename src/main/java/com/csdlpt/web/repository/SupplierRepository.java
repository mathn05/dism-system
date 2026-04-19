package com.csdlpt.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, String> {

    List<Supplier> findAllByOrderByIdAsc();
}
