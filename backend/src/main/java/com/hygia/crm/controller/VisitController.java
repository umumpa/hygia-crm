package com.hygia.crm.controller;

import com.hygia.crm.dto.VisitCreateDto;
import com.hygia.crm.dto.VisitDto;
import com.hygia.crm.entity.Customer;
import com.hygia.crm.entity.VisitLog;
import com.hygia.crm.repository.CustomerRepository;
import com.hygia.crm.repository.VisitLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/customers/{customerId}/visits")
@Tag(name = "Visits", description = "Visit log management API")
public class VisitController {

    private final VisitLogRepository visitLogRepository;
    private final CustomerRepository customerRepository;

    public VisitController(VisitLogRepository visitLogRepository, CustomerRepository customerRepository) {
        this.visitLogRepository = visitLogRepository;
        this.customerRepository = customerRepository;
    }

    @PostMapping
    @Operation(summary = "Create a visit log", description = "Creates a new visit log for the specified customer. Returns 404 if customer not found.")
    public ResponseEntity<?> createVisit(
            @Parameter(description = "Customer ID", required = true) @PathVariable Long customerId,
            @Valid @RequestBody VisitCreateDto createDto) {

        // Validate nextFollowUpAt >= visitAt if present
        if (createDto.getNextFollowUpAt() != null && createDto.getVisitAt() != null) {
            if (createDto.getNextFollowUpAt().isBefore(createDto.getVisitAt())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("nextFollowUpAt must be greater than or equal to visitAt");
            }
        }

        // Check if customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElse(null);

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer with ID " + customerId + " not found");
        }

        // Create visit log
        VisitLog visitLog = new VisitLog();
        visitLog.setCustomer(customer);
        visitLog.setVisitAt(createDto.getVisitAt());
        visitLog.setType(createDto.getType());
        visitLog.setResult(createDto.getResult());
        visitLog.setNotes(createDto.getNotes());
        visitLog.setNextFollowUpAt(createDto.getNextFollowUpAt());

        // Save visit log
        VisitLog savedVisitLog = visitLogRepository.save(visitLog);

        // Convert to DTO
        VisitDto visitDto = convertToDto(savedVisitLog);

        return ResponseEntity.status(HttpStatus.CREATED).body(visitDto);
    }

    @GetMapping
    @Operation(
        summary = "Get visit logs for a customer",
        description = "Returns a paginated list of visit logs for the specified customer, sorted by visitAt descending by default. " +
                     "Returns a Page structure with content, pageable, totalElements, and totalPages. " +
                     "Default pagination: page=0, size=20, sort=visitAt,desc"
    )
    public ResponseEntity<?> getVisits(
            @Parameter(description = "Customer ID", required = true) @PathVariable Long customerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction") @RequestParam(defaultValue = "visitAt,desc") String sort) {

        // Check if customer exists
        if (!customerRepository.existsById(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer with ID " + customerId + " not found");
        }

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;

        // Create pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // Get visit logs
        Page<VisitLog> visitLogsPage = visitLogRepository.findByCustomerId(customerId, pageable);

        // Convert to DTOs - return Page structure for consistency with customers endpoint
        Page<VisitDto> visitDtoPage = visitLogsPage.map(this::convertToDto);

        return ResponseEntity.ok(visitDtoPage);
    }

    private VisitDto convertToDto(VisitLog visitLog) {
        VisitDto dto = new VisitDto();
        dto.setId(visitLog.getId());
        dto.setVisitAt(visitLog.getVisitAt());
        dto.setType(visitLog.getType());
        dto.setResult(visitLog.getResult());
        dto.setNotes(visitLog.getNotes());
        dto.setNextFollowUpAt(visitLog.getNextFollowUpAt());
        return dto;
    }
}

