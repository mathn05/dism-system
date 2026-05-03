package com.csdlpt.web.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.csdlpt.web.entity.AppUser;
import com.csdlpt.web.entity.ImportDetail;
import com.csdlpt.web.entity.ImportDetailId;
import com.csdlpt.web.entity.ImportEntity;
import com.csdlpt.web.entity.Product;
import com.csdlpt.web.entity.Station;
import com.csdlpt.web.entity.Supplier;
import com.csdlpt.web.repository.AppUserRepository;
import com.csdlpt.web.repository.ImportDetailRepository;
import com.csdlpt.web.repository.ImportRepository;
import com.csdlpt.web.repository.ProductRepository;
import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.repository.SupplierRepository;

@Service
@Transactional
public class ImportService {

    public record ImportLineItem(String productId, int quantity, BigDecimal importPrice) {}

    public record ImportView(ImportEntity importEntity, List<ImportDetail> details) {}

    private final ImportRepository importRepository;
    private final ImportDetailRepository importDetailRepository;
    private final AppUserRepository appUserRepository;
    private final SupplierRepository supplierRepository;
    private final StationRepository stationRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final IdGenerationService idGenerationService;

    public ImportService(ImportRepository importRepository,
                         ImportDetailRepository importDetailRepository,
                         AppUserRepository appUserRepository,
                         SupplierRepository supplierRepository,
                         StationRepository stationRepository,
                         ProductRepository productRepository,
                         InventoryService inventoryService,
                         IdGenerationService idGenerationService) {
        this.importRepository = importRepository;
        this.importDetailRepository = importDetailRepository;
        this.appUserRepository = appUserRepository;
        this.supplierRepository = supplierRepository;
        this.stationRepository = stationRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.idGenerationService = idGenerationService;
    }

    @Transactional(readOnly = true)
    public List<ImportView> getStationImports(String stationId) {
        return importRepository.findByStation_IdOrderByCreatedAtDescIdDesc(normalizeRequired(stationId, "Station is required"))
            .stream()
            .map(importEntity -> new ImportView(importEntity, importDetailRepository.findByImportEntity_IdOrderByProduct_IdAsc(importEntity.getId())))
            .toList();
    }

    public ImportEntity createImport(String userId, String stationId, String supplierId, List<ImportLineItem> lineItems) {
        String normalizedUserId = normalizeRequired(userId, "User is required");
        String normalizedStationId = normalizeRequired(stationId, "Station is required");
        String normalizedSupplierId = normalizeRequired(supplierId, "Supplier is required");
        List<ImportLineItem> normalizedItems = normalizeLineItems(lineItems);

        AppUser user = appUserRepository.findById(normalizedUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Station station = stationRepository.findById(normalizedStationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found"));
        Supplier supplier = supplierRepository.findById(normalizedSupplierId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));

        if (!station.getId().equalsIgnoreCase(user.getStation().getId())) {
            throw new IllegalArgumentException("User is not allowed to create imports for another station");
        }

        ImportEntity importEntity = new ImportEntity();
        importEntity.setId(idGenerationService.nextImportId(station.getId()));
        importEntity.setCreatedAt(LocalDateTime.now());
        importEntity.setUser(user);
        importEntity.setSupplier(supplier);
        importEntity.setStation(station);
        ImportEntity savedImport = importRepository.save(importEntity);

        for (ImportLineItem lineItem : normalizedItems) {
            Product product = productRepository.findById(lineItem.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + lineItem.productId()));

            ImportDetail detail = new ImportDetail();
            detail.setId(new ImportDetailId(savedImport.getId(), product.getId()));
            detail.setImportEntity(savedImport);
            detail.setProduct(product);
            detail.setQuantity(lineItem.quantity());
            detail.setImportPrice(lineItem.importPrice());
            importDetailRepository.save(detail);

            inventoryService.adjustQuantity(station.getId(), product.getId(), lineItem.quantity());
        }

        return savedImport;
    }

    private List<ImportLineItem> normalizeLineItems(List<ImportLineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("At least one import item is required");
        }

        List<ImportLineItem> normalized = new ArrayList<>();
        Set<String> seenProductIds = new HashSet<>();
        for (ImportLineItem lineItem : lineItems) {
            if (lineItem == null) {
                continue;
            }

            String productId = normalizeRequired(lineItem.productId(), "Product ID is required");
            if (lineItem.quantity() <= 0) {
                throw new IllegalArgumentException("Import quantity must be greater than zero");
            }
            if (lineItem.importPrice() == null || lineItem.importPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Import price must be zero or greater");
            }
            if (!seenProductIds.add(productId)) {
                throw new IllegalArgumentException("Each product can only appear once in an import receipt");
            }
            normalized.add(new ImportLineItem(productId, lineItem.quantity(), lineItem.importPrice()));
        }

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("At least one import item is required");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
