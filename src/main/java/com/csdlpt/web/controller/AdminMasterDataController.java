package com.csdlpt.web.controller;

import com.csdlpt.web.entity.Category;
import com.csdlpt.web.entity.Product;
import com.csdlpt.web.entity.Supplier;
import com.csdlpt.web.service.AdminMasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/masterdata")
@RequiredArgsConstructor
public class AdminMasterDataController {

    private final AdminMasterDataService adminMasterDataService;

    @GetMapping
    public String index(Model model) {

        model.addAttribute("products", adminMasterDataService.getAllProducts());
        model.addAttribute("categories", adminMasterDataService.getAllCategories());
        model.addAttribute("suppliers", adminMasterDataService.getAllSuppliers());

        model.addAttribute("productCount", adminMasterDataService.countProducts());
        model.addAttribute("categoryCount", adminMasterDataService.countCategories());
        model.addAttribute("supplierCount", adminMasterDataService.countSuppliers());

        model.addAttribute("product", new Product());
        model.addAttribute("category", new Category());
        model.addAttribute("supplier", new Supplier());

        return "admin-masterdata";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product) {

        adminMasterDataService.saveProduct(product);

        return "redirect:/admin/masterdata";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable String id) {

        adminMasterDataService.deleteProduct(id);

        return "redirect:/admin/masterdata";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category) {

        adminMasterDataService.saveCategory(category);

        return "redirect:/admin/masterdata";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable String id) {

        adminMasterDataService.deleteCategory(id);

        return "redirect:/admin/masterdata";
    }

    @PostMapping("/suppliers/save")
    public String saveSupplier(@ModelAttribute Supplier supplier) {

        adminMasterDataService.saveSupplier(supplier);

        return "redirect:/admin/masterdata";
    }

    @GetMapping("/suppliers/delete/{id}")
    public String deleteSupplier(@PathVariable String id) {

        adminMasterDataService.deleteSupplier(id);

        return "redirect:/admin/masterdata";
    }
}