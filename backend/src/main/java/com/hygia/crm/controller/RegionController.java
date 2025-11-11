package com.hygia.crm.controller;

import com.hygia.crm.dto.RegionDto;
import com.hygia.crm.entity.Region;
import com.hygia.crm.repository.RegionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/regions")
@Tag(name = "Regions", description = "Region management API")
public class RegionController {

    private final RegionRepository regionRepository;

    public RegionController(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @GetMapping
    @Operation(summary = "Get all regions", description = "Returns a list of all regions sorted by name in ascending order")
    public ResponseEntity<List<RegionDto>> getAllRegions() {
        List<Region> regions = regionRepository.findAllByOrderByNameAsc();
        List<RegionDto> regionDtos = regions.stream()
                .map(region -> new RegionDto(region.getId(), region.getName(), region.getState()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(regionDtos);
    }
}

