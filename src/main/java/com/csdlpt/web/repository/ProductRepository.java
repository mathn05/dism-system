package com.csdlpt.web.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.csdlpt.web.entity.Product;
public interface ProductRepository extends JpaRepository<Product, String> {

}
