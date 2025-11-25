package com.hygia.crm.controller;

import com.hygia.crm.dto.ErrorResponse;
import com.hygia.crm.dto.ProductCreateDto;
import com.hygia.crm.dto.ProductDto;
import com.hygia.crm.entity.Product;
import com.hygia.crm.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management API")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product. Returns 409 CONFLICT if product with same itemCode already exists.")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductCreateDto createDto) {
        // Check if product with same itemCode already exists
        if (productRepository.existsByItemCode(createDto.getItemCode())) {
            ErrorResponse error = new ErrorResponse(
                "PRODUCT_ALREADY_EXISTS",
                "Product with item code '" + createDto.getItemCode() + "' already exists"
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // Create product entity
        Product product = new Product();
        product.setItemCode(createDto.getItemCode());
        product.setDescription(createDto.getDescription());
        product.setDefaultUnitPrice(createDto.getDefaultUnitPrice());
        product.setCompanyTag(createDto.getCompanyTag());
        product.setProductType(createDto.getProductType());
        product.setBarcode(createDto.getBarcode());
        product.setActive(true); // Default active = true

        // Save product
        Product savedProduct = productRepository.save(product);

        // Convert to DTO
        ProductDto productDto = convertToDto(savedProduct);

        return ResponseEntity.status(HttpStatus.CREATED).body(productDto);
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Returns a list of all products sorted by itemCode ascending")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDto> productDtos = products.stream()
                .sorted(Comparator.comparing(Product::getItemCode))
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDtos);
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setItemCode(product.getItemCode());
        dto.setDescription(product.getDescription());
        dto.setDefaultUnitPrice(product.getDefaultUnitPrice());
        dto.setCompanyTag(product.getCompanyTag());
        dto.setProductType(product.getProductType());
        dto.setBarcode(product.getBarcode());
        dto.setActive(product.getActive());
        return dto;
    }
}

