package com.csdlpt.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.Product;

public interface ProductRepository extends JpaRepository<Product, String> {

    @EntityGraph(attributePaths = {"category"})
    List<Product> findAllByOrderByIdAsc();
}
