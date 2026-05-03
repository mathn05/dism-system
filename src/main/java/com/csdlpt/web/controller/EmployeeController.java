package com.csdlpt.web.controller;

import com.csdlpt.web.entity.AppUser;
import com.csdlpt.web.entity.Station;
import com.csdlpt.web.repository.AppUserRepository;
import com.csdlpt.web.repository.StationRepository;
import com.csdlpt.web.service.IdGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/admin/users")
public class EmployeeController {

    private final StationRepository stationRepository;
    private final AppUserRepository appUserRepository;
    private final IdGenerationService idGenerationService;

    public EmployeeController(StationRepository stationRepository,
                              AppUserRepository appUserRepository,
                              IdGenerationService idGenerationService) {
        this.stationRepository = stationRepository;
        this.appUserRepository = appUserRepository;
        this.idGenerationService = idGenerationService;
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute AppUser user,
                             @RequestParam String stationId) {

        user.setStation(stationRepository.findById(stationId).orElseThrow());
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(idGenerationService.nextUserId(stationId));
        } else {
            user.setId(idGenerationService.normalizeManualId(user.getId()));
        }
        user.setPassword(user.getPassword());
        appUserRepository.save(user);

        return "redirect:/admin/users";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute AppUser user,
                             @RequestParam String stationId,
                             RedirectAttributes redirectAttributes) {
        try {
            AppUser existingUser = appUserRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            existingUser.setUsername(user.getUsername());
            existingUser.setFullname(user.getFullname());
            existingUser.setPhoneNumber(user.getPhoneNumber());
            existingUser.setRole(user.getRole());

            Station station = stationRepository.findById(stationId)
                    .orElseThrow(() -> new RuntimeException("Station not found"));

            existingUser.setStation(station);

            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }

            appUserRepository.save(existingUser);

            redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id) {
        appUserRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    @GetMapping
    public String listUsers(
            @RequestParam(required = false) String stationId,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        List<AppUser> users;

        if (stationId != null && !stationId.isEmpty()) {
            users = appUserRepository.findByStationId(stationId);
        } else {
            users = appUserRepository.findAll();
        }

        if (keyword != null && !keyword.isEmpty()) {
            users = appUserRepository.search(stationId, keyword);
        }

        model.addAttribute("users", users);
        model.addAttribute("stations", stationRepository.findAll());
        model.addAttribute("selectedStationId", stationId);
        model.addAttribute("keyword", keyword);

        return "employee-management";
    }
}
