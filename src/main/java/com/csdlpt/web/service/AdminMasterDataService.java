package com.csdlpt.web.service;

import com.csdlpt.web.entity.Category;
import com.csdlpt.web.entity.Product;
import com.csdlpt.web.entity.Supplier;
import com.csdlpt.web.repository.CategoryRepository;
import com.csdlpt.web.repository.ProductRepository;
import com.csdlpt.web.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMasterDataService{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final IdGenerationService idGenerationService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public void saveProduct(Product product) {
        if (product.getId() == null || product.getId().isBlank()) {
            product.setId(idGenerationService.nextProductId());
        } else {
            product.setId(idGenerationService.normalizeManualId(product.getId()));
        }
        productRepository.save(product);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    public long countProducts() {
        return productRepository.count();
    }


    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }


    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    public void deleteCategory(String id) {
        categoryRepository.deleteById(id);
    }

    public long countCategories() {
        return categoryRepository.count();
    }


    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public void saveSupplier(Supplier supplier) {
        if (supplier.getId() == null || supplier.getId().isBlank()) {
            supplier.setId(idGenerationService.nextSupplierId());
        } else {
            supplier.setId(idGenerationService.normalizeManualId(supplier.getId()));
        }
        supplierRepository.save(supplier);
    }

    public void deleteSupplier(String id) {
        supplierRepository.deleteById(id);
    }

    public long countSuppliers() {
        return supplierRepository.count();
    }
}
