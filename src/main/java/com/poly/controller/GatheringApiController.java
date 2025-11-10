package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poly.dto.ApiResponse;
import com.poly.dto.GatheringResponseDTO;
import com.poly.service.GatheringService;

@RestController
@RequestMapping("/api/gathering")
public class GatheringApiController {

    @Autowired
    private GatheringService gatheringService;

    @PostMapping("/gather")
    public ApiResponse<GatheringResponseDTO> gather(@RequestParam String resourceType) {
        try {
            return gatheringService.doGather(resourceType);
        } catch (Exception e) {
            // Log the exception
            return ApiResponse.error("Lỗi khi thu thập: " + e.getMessage());
        }
    }
}