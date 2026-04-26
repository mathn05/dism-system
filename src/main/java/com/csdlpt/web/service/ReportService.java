package com.csdlpt.web.service;

import com.csdlpt.web.dto.InventoryByStationDTO;
import com.csdlpt.web.repository.ImportRepository;
import com.csdlpt.web.repository.InventoryRepository;
import com.csdlpt.web.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;
    private final ImportRepository importRepository;
    private final InventoryRepository inventoryRepository;

    public Long totalInventory(String stationId) {
        Long val;

        if (hasStation(stationId)) {
            val = inventoryRepository.getTotalInventoryByStation(stationId);
        } else {
            val = inventoryRepository.getTotalInventory();
        }

        return val == null ? 0L : val;
    }

    public List<InventoryByStationDTO> inventoryByStation(String stationId) {

        List<Object[]> raw = hasStation(stationId)
                ? inventoryRepository.inventoryByStation(stationId)
                : inventoryRepository.inventoryByStation();

        long total = totalInventory(stationId);
        if (total == 0) total = 1;

        List<InventoryByStationDTO> result = new ArrayList<>();

        for (Object[] r : raw) {
            String station = (String) r[0];
            long qty = ((Number) r[1]).longValue();
            double percent = (qty * 100.0) / total;

            result.add(new InventoryByStationDTO(station, qty, percent));
        }

        return result;
    }

    public List<String> importLabels(String stationId, LocalDate fromDate, LocalDate toDate) {

        List<?> raw;

        if (hasStation(stationId)) {
            if (fromDate != null && toDate != null) {
                raw = importRepository.importLabels(stationId, fromDate, toDate);
            } else {
                raw = importRepository.importLabels(stationId);
            }
        } else {
            if (fromDate != null && toDate != null) {
                raw = importRepository.importLabels(fromDate, toDate);
            } else {
                raw = importRepository.importLabels();
            }
        }

        return raw.stream()
                .map(Object::toString)
                .toList();
    }

    public List<Long> importData(String stationId, LocalDate fromDate, LocalDate toDate) {
        if (hasStation(stationId)) {
            if (fromDate != null && toDate != null) {
                return importRepository.importData(stationId, fromDate, toDate);
            } else {
                return importRepository.importData(stationId);
            }
        }

        if (fromDate != null && toDate != null) {
            return importRepository.importData(fromDate, toDate);
        }
        return importRepository.importData();
    }

    public List<Long> saleData(String stationId, LocalDate fromDate, LocalDate toDate) {
        if (hasStation(stationId)) {
            if (fromDate != null && toDate != null) {
                return saleRepository.saleData(stationId, fromDate, toDate);
            } else {
                return saleRepository.saleData(stationId);
            }
        }

        if (fromDate != null && toDate != null) {
            return saleRepository.saleData(fromDate, toDate);
        }

        return saleRepository.saleData();
    }

    public List<String> revenueLabels(String stationId, LocalDate fromDate, LocalDate toDate) {
        if (hasStation(stationId)) {
            if (fromDate != null && toDate != null) {
                return saleRepository.revenueLabels(stationId, fromDate, toDate)
                        .stream()
                        .map(r -> r[0].toString())
                        .toList();
            } else {
                return saleRepository.revenueLabels(stationId)
                        .stream()
                        .map(r -> r[0].toString())
                        .toList();
            }
        }

        if (fromDate != null && toDate != null) {
            return saleRepository.revenueLabels(fromDate, toDate)
                    .stream()
                    .map(Object::toString)
                    .toList();
        }

        return saleRepository.revenueLabels()
                .stream()
                .map(Object::toString)
                .toList();
    }

    public List<Long> revenueData(String stationId, LocalDate fromDate, LocalDate toDate) {
        if (hasStation(stationId)) {
            if (fromDate != null && toDate != null) {
                return saleRepository.revenueData(stationId, fromDate, toDate);
            } else {
                return saleRepository.revenueData(stationId);
            }
        }

        if (fromDate != null && toDate != null) {
            return saleRepository.revenueData(fromDate, toDate);
        }

        return saleRepository.revenueData();
    }

    public List<String> costLabels(String stationId, LocalDate fromDate, LocalDate toDate) {
        if (hasStation(stationId)) {
            if (fromDate != null && toDate != null) {
                return importRepository.costLabels(stationId, fromDate, toDate)
                        .stream()
                        .map(r -> r[0].toString())
                        .toList();
            } else {
                return importRepository.costLabels(stationId)
                        .stream()
                        .map(r -> r[0].toString())
                        .toList();
            }
        }

        if (fromDate != null && toDate != null) {
            return importRepository.costLabels(fromDate, toDate)
                    .stream()
                    .map(Object::toString)
                    .toList();
        }

        return importRepository.costLabels()
                .stream()
                .map(Object::toString)
                .toList();
    }

    public List<Long> costData(String stationId, LocalDate fromDate, LocalDate toDate) {
        if (hasStation(stationId)) {
            if (fromDate != null && toDate != null) {
                return importRepository.costData(stationId, fromDate, toDate);
            } else {
                return importRepository.costData(stationId);
            }
        }

        if (fromDate != null && toDate != null) {
            return importRepository.costData(fromDate, toDate);
        }

        return importRepository.costData();
    }

    public Long totalRevenue(String stationId, LocalDate fromDate, LocalDate toDate) {
        Long val;

        if (hasStation(stationId)) {
            if (fromDate != null && toDate != null) {
                val = saleRepository.totalRevenueByStation(stationId, fromDate, toDate);
            } else {
                val = saleRepository.totalRevenueByStation(stationId);
            }
        } else {
            if (fromDate != null && toDate != null) {
                val = saleRepository.totalRevenue(fromDate, toDate);
            } else {
                val = saleRepository.totalRevenue();
            }
        }

        return val == null ? 0L : val;
    }

    public Long totalCost(String stationId, LocalDate fromDate, LocalDate toDate) {

        if(hasStation(stationId)){
            if(fromDate !=  null && toDate != null){
                return importRepository.totalCostByStation(stationId, fromDate, toDate);
            }else{
                return importRepository.totalCostByStation(stationId);
            }

        }
        if(fromDate !=  null && toDate != null){
            return importRepository.totalCost(fromDate, toDate);
        }
        return importRepository.totalCost();

    }

    public Long profit(String stationId, LocalDate fromDate, LocalDate toDate) {
        if(hasStation(stationId)){
            if(fromDate !=  null && toDate != null){
                 return saleRepository.profitByStation(stationId, fromDate, toDate);
            }else{
                return saleRepository.profitByStation(stationId);
            }

        }
        if(fromDate !=  null && toDate != null){
            return saleRepository.profit(fromDate, toDate);
        }
        return saleRepository.profit();
    }

    public Long totalOrders(String stationId, LocalDate fromDate, LocalDate toDate) {

        Long val;

        if (hasStation(stationId)) {
            if (fromDate != null && toDate != null) {
                val = saleRepository.totalOrdersByStation(stationId, fromDate, toDate);
            } else {
                val = saleRepository.totalOrdersByStation(stationId);
            }
        } else {
            if (fromDate != null && toDate != null) {
                val = saleRepository.totalOrders(fromDate, toDate);
            } else {
                val = saleRepository.totalOrders();
            }
        }

        return val == null ? 0L : val;
    }

    private boolean hasStation(String stationId) {
        return stationId != null && !stationId.isEmpty();
    }
}