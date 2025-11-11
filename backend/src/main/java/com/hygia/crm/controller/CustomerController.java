package com.hygia.crm.controller;

import com.hygia.crm.dto.CustomerCreateDto;
import com.hygia.crm.dto.CustomerDto;
import com.hygia.crm.dto.CustomerRegionDto;
import com.hygia.crm.dto.ErrorResponse;
import com.hygia.crm.entity.Customer;
import com.hygia.crm.entity.Region;
import com.hygia.crm.repository.CustomerRepository;
import com.hygia.crm.repository.CustomerSpecification;
import com.hygia.crm.repository.RegionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management API")
public class CustomerController {

    private static final Set<String> VALID_TIERS = new HashSet<>(Arrays.asList("A", "B", "C", "Potential"));

    private final CustomerRepository customerRepository;
    private final RegionRepository regionRepository;

    public CustomerController(CustomerRepository customerRepository, RegionRepository regionRepository) {
        this.customerRepository = customerRepository;
        this.regionRepository = regionRepository;
    }

    @PostMapping
    @Operation(
        summary = "Create a new customer",
        description = "Creates a new customer. Returns 409 CONFLICT if customer with same nameStd already exists. " +
                     "isProspect is automatically calculated from tier (tier='Potential' → isProspect=true, otherwise false). " +
                     "If tier is not provided, defaults to 'Potential'."
    )
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerCreateDto createDto) {
        // Check if customer with same nameStd already exists
        if (customerRepository.existsByNameStd(createDto.getNameStd())) {
            ErrorResponse error = new ErrorResponse(
                "CUSTOMER_ALREADY_EXISTS",
                "Customer with name '" + createDto.getNameStd() + "' already exists"
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // Find region by ID
        Region region = regionRepository.findById(createDto.getRegionId())
                .orElse(null);
        
        if (region == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Region with ID " + createDto.getRegionId() + " not found");
        }

        // Determine tier (use from DTO if provided, otherwise default to "Potential")
        String tier = createDto.getTier();
        if (tier == null || tier.isEmpty()) {
            tier = "Potential";
        }

        // Validate tier if provided
        if (!VALID_TIERS.contains(tier)) {
            ErrorResponse error = new ErrorResponse(
                "INVALID_TIER",
                "Invalid tier value: " + tier + ". Allowed values are: A, B, C, Potential"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Create customer entity
        Customer customer = new Customer();
        customer.setNameStd(createDto.getNameStd());
        // isProspect is ignored from request - will be automatically set from tier by @PrePersist
        customer.setRegion(region);
        customer.setAddressText(createDto.getAddressText());
        customer.setPhone(createDto.getPhone());
        customer.setEmail(createDto.getEmail());
        customer.setPaymentTerms(createDto.getPaymentTerms());
        customer.setNotes(createDto.getNotes());
        customer.setTier(tier);
        // isProspect will be automatically set to true if tier == "Potential", false otherwise
        // This happens in the @PrePersist lifecycle callback

        // Save customer
        Customer savedCustomer = customerRepository.save(customer);

        // Convert to DTO
        CustomerDto customerDto = convertToDto(savedCustomer);

        return ResponseEntity.status(HttpStatus.CREATED).body(customerDto);
    }

    @GetMapping
    @Operation(
        summary = "Get customers with pagination and filters",
        description = "Returns a paginated list of customers with optional filters. " +
                     "Supports filtering by regionId, tier, search query (q), and isProspect. " +
                     "Supports follow-up filtering with followup=due. " +
                     "Default pagination: page=0, size=20, sort=nameStd,asc"
    )
    public ResponseEntity<?> getAllCustomers(
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size", example = "20") 
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort field and direction (format: field,direction)", example = "nameStd,asc") 
            @RequestParam(defaultValue = "nameStd,asc") String sort,
            
            @Parameter(description = "Filter by region ID") 
            @RequestParam(required = false) Long regionId,
            
            @Parameter(description = "Filter by tier (allowed values: A, B, C, Potential)") 
            @RequestParam(required = false) String tier,
            
            @Parameter(description = "Search query (case-insensitive match on nameStd and phone)") 
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Filter by prospect status (true/false)") 
            @RequestParam(required = false) Boolean isProspect,

            @Parameter(description = "Filter by follow-up status. Currently only 'due' is supported.") 
            @RequestParam(required = false) String followup) {

        // Validate tier value
        if (tier != null && !tier.isEmpty() && !VALID_TIERS.contains(tier)) {
            ErrorResponse error = new ErrorResponse(
                "INVALID_TIER",
                "Invalid tier value: " + tier + ". Allowed values are: A, B, C, Potential"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;

        // Create pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // Normalize follow-up filter
        String followupFilter = (followup != null && "due".equalsIgnoreCase(followup)) ? "due" : null;

        // Build specification with filters
        Specification<Customer> spec = CustomerSpecification.withFilters(regionId, tier, q, isProspect, followupFilter);

        // Get paginated customers
        Page<Customer> customerPage = customerRepository.findAll(spec, pageable);

        // Convert to DTOs
        Page<CustomerDto> customerDtoPage = customerPage.map(this::convertToDto);

        return ResponseEntity.ok(customerDtoPage);
    }

    private CustomerDto convertToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setNameStd(customer.getNameStd());
        // Compute isProspect from tier for consistency
        // tier == "Potential" → isProspect = true, otherwise false
        String tier = customer.getTier();
        dto.setIsProspect(tier != null && "Potential".equalsIgnoreCase(tier));
        dto.setAddressText(customer.getAddressText());
        dto.setPhone(customer.getPhone());
        dto.setEmail(customer.getEmail());
        dto.setPaymentTerms(customer.getPaymentTerms());
        dto.setTier(customer.getTier());

        // Convert region to DTO
        Region region = customer.getRegion();
        if (region != null) {
            CustomerRegionDto regionDto = new CustomerRegionDto();
            regionDto.setId(region.getId());
            regionDto.setName(region.getName());
            dto.setRegion(regionDto);
        }

        return dto;
    }
}

