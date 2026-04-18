package com.csdlpt.web.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.csdlpt.web.entity.Inventory;
import com.csdlpt.web.entity.InventoryId;
import com.csdlpt.web.entity.Product;
import com.csdlpt.web.entity.Station;
import com.csdlpt.web.repository.InventoryRepository;
import com.csdlpt.web.repository.ProductRepository;
import com.csdlpt.web.repository.StationRepository;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StationRepository stationRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            StationRepository stationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional(readOnly = true)
    public List<Inventory> getStationInventory(String stationId) {
        return inventoryRepository.findByStation_IdOrderByProduct_IdAsc(normalizeStationId(stationId));
    }

    @Transactional(readOnly = true)
    public long getTrackedProductCount(String stationId) {
        return inventoryRepository.countByStation_Id(normalizeStationId(stationId));
    }

    @Transactional(readOnly = true)
    public long getLowStockCount(String stationId, int threshold) {
        return inventoryRepository.countByStation_IdAndQuantityLessThanEqual(normalizeStationId(stationId), threshold);
    }

    @Transactional(readOnly = true)
    public long getTotalQuantity(String stationId) {
        return inventoryRepository.sumQuantityByStationId(normalizeStationId(stationId));
    }

    public Inventory receiveStock(String stationId, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        return adjustQuantity(stationId, productId, quantity);
    }

    public Inventory adjustQuantity(String stationId, String productId, int delta) {
        if (delta == 0) {
            throw new IllegalArgumentException("Adjustment cannot be zero");
        }

        String normalizedStationId = normalizeStationId(stationId);
        String normalizedProductId = normalizeProductId(productId);

        Inventory inventory = inventoryRepository.findByStation_IdAndProduct_Id(normalizedStationId, normalizedProductId)
            .orElse(null);

        if (inventory == null) {
            if (delta < 0) {
                throw new IllegalArgumentException("Cannot reduce stock for a product that does not exist in this station");
            }
            inventory = createInventory(normalizedStationId, normalizedProductId);
        }

        int nextQuantity = inventory.getQuantity() + delta;
        if (nextQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot become negative");
        }

        inventory.setQuantity(nextQuantity);
        return inventoryRepository.save(inventory);
    }

    private Inventory createInventory(String stationId, String productId) {
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found"));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        return new Inventory(new InventoryId(station.getId(), product.getId()), station, product, 0);
    }

    private String normalizeStationId(String stationId) {
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("Station is required");
        }
        return stationId.trim();
    }

    private String normalizeProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        return productId.trim();
    }
}


