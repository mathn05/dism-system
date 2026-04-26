package com.csdlpt.web.controller;


import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final StationRepository  stationRepository;

    @GetMapping
    public String report(
            @RequestParam(required = false) String stationId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Model model) {

        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        model.addAttribute("selectedStation", stationId);
        model.addAttribute("stations", stationRepository.findAll());

        model.addAttribute("totalRevenue",
                reportService.totalRevenue(stationId, fromDate, toDate));

        model.addAttribute("totalCost",
                reportService.totalCost(stationId, fromDate, toDate));

        model.addAttribute("profit",
                reportService.profit(stationId, fromDate, toDate));

        model.addAttribute("totalOrders",
                reportService.totalOrders(stationId, fromDate, toDate));

        model.addAttribute("totalInventory",
                reportService.totalInventory(stationId));

        model.addAttribute("revenueLabels",
                reportService.revenueLabels(stationId, fromDate, toDate));

        model.addAttribute("revenueData",
                reportService.revenueData(stationId, fromDate, toDate));

        model.addAttribute("costLabels",
                reportService.costLabels(stationId, fromDate, toDate));

        model.addAttribute("costData",
                reportService.costData(stationId, fromDate, toDate));

        model.addAttribute("importLabels",
                reportService.importLabels(stationId, fromDate, toDate));

        model.addAttribute("importData",
                reportService.importData(stationId, fromDate, toDate));

        model.addAttribute("saleData",
                reportService.saleData(stationId, fromDate, toDate));

        model.addAttribute("inventoryByStation",
                reportService.inventoryByStation(stationId));


        return "admin-report";
    }
}