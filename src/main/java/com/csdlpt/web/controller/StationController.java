package com.csdlpt.web.controller;

import com.csdlpt.web.entity.Station;
import com.csdlpt.web.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/stations")
public class StationController {

    private final StationService stationService;

    @PostMapping("/create")
    public String create(@ModelAttribute Station station,
                         @RequestParam(value = "headquarter", required = false) Boolean headquarter) {

        station.setHeadquarter(headquarter != null && headquarter);

        stationService.create(station);
        return "redirect:/admin/stations";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Station station) {
        stationService.update(station);
        return "redirect:/admin/stations";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable String id) {
        stationService.delete(id);
        return "redirect:/admin/stations";
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       Model model) {

        model.addAttribute("stations", stationService.search(keyword));
        model.addAttribute("keyword", keyword);

        return "admin-station";
    }
}