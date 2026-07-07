package net.anjali.childcare.controller;


import net.anjali.childcare.dto.response.CaregiverResponse;
import net.anjali.childcare.service.CaregiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/parent")
public class ParentController {

    @Autowired
    private CaregiverService caregiverService;

    // Parent searches caregivers by city and date
    @GetMapping("/search")
    public ResponseEntity<List<CaregiverResponse>> searchCaregivers(
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(caregiverService.searchCaregivers(city, date));
    }
}