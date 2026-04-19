package com.csdlpt.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findAllByOrderByIdAsc();
}
