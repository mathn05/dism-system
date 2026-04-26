package com.csdlpt.web.controller;

import com.csdlpt.web.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
        model.addAttribute("totalOrders", dashboardService.getTotalOrders());
        model.addAttribute("totalInventory", dashboardService.getTotalInventory());
        model.addAttribute("lowStockCount", dashboardService.getLowStockCount());

        model.addAttribute("revenueLabels", dashboardService.getRevenueLabels());
        model.addAttribute("revenueData", dashboardService.getRevenueData());

        model.addAttribute("orderLabels", dashboardService.getOrderLabels());
        model.addAttribute("orderData", dashboardService.getOrderData());

        model.addAttribute("topStations", dashboardService.getTopStations());
        model.addAttribute("topProducts", dashboardService.getTopProducts());

        return "admin-dashboard";
    }

}
