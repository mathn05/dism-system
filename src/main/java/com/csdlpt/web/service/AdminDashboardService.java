package com.csdlpt.web.service;

import com.csdlpt.web.dto.TopProductDto;
import com.csdlpt.web.dto.TopStationDto;
import com.csdlpt.web.repository.ImportRepository;
import com.csdlpt.web.repository.InventoryRepository;
import com.csdlpt.web.repository.SaleDetailRepository;
import com.csdlpt.web.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final SaleRepository saleRepository;
    private final ImportRepository importRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final InventoryRepository inventoryRepository;

    public BigDecimal getTotalRevenue() {
        return saleDetailRepository.getTotalRevenue();
    }

    public Long getTotalOrders() {
        return saleRepository.count() + importRepository.count();
    }

    public Integer getTotalInventory() {
        return inventoryRepository.sumAllQuantity();
    }

    public Long getLowStockCount() {
        return inventoryRepository.countLowStock(10);
    }

    public List<String> getRevenueLabels() {
        List<String> labels = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            labels.add(LocalDate.now().minusDays(i).toString());
        }

        return labels;
    }

    public List<BigDecimal> getRevenueData() {
        List<BigDecimal> data = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            data.add(saleDetailRepository.getRevenueByDate(date));
        }

        return data;
    }

    public List<String> getOrderLabels() {
        return getRevenueLabels();
    }

    public List<Long> getOrderData() {
        List<Long> data = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            data.add(saleRepository.countByDate(date));
        }

        return data;
    }

    public List<TopStationDto> getTopStations() {
        return saleRepository.getTopStations();
    }

    public List<TopProductDto> getTopProducts() {
        return saleDetailRepository.getTopProducts();
    }
}