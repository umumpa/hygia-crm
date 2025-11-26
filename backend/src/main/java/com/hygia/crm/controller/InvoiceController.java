package com.hygia.crm.controller;

import com.hygia.crm.dto.ErrorResponse;
import com.hygia.crm.dto.InvoiceCreateDto;
import com.hygia.crm.dto.InvoiceDto;
import com.hygia.crm.dto.InvoiceItemCreateDto;
import com.hygia.crm.dto.InvoiceItemDto;
import com.hygia.crm.entity.Customer;
import com.hygia.crm.entity.Invoice;
import com.hygia.crm.entity.InvoiceItem;
import com.hygia.crm.entity.Product;
import com.hygia.crm.repository.CustomerRepository;
import com.hygia.crm.repository.InvoiceItemRepository;
import com.hygia.crm.repository.InvoiceRepository;
import com.hygia.crm.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Invoice management API")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public InvoiceController(
            InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new invoice with items", 
               description = "Creates a new invoice with line items. Validates customer and products exist, " +
                           "uses product default price if unitPrice not provided, and calculates total amount.")
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceCreateDto createDto) {
        // Validate customer exists
        Customer customer = customerRepository.findById(createDto.getCustomerId())
                .orElse(null);
        
        if (customer == null) {
            ErrorResponse error = new ErrorResponse(
                "CUSTOMER_NOT_FOUND",
                "Customer with ID " + createDto.getCustomerId() + " not found"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // Validate all products exist and collect them
        List<Product> products = new ArrayList<>();
        for (InvoiceItemCreateDto itemDto : createDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElse(null);
            
            if (product == null) {
                ErrorResponse error = new ErrorResponse(
                    "PRODUCT_NOT_FOUND",
                    "Product with ID " + itemDto.getProductId() + " not found"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            products.add(product);
        }

        // Create invoice entity
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(createDto.getInvoiceNumber());
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(createDto.getInvoiceDate());
        invoice.setNote(createDto.getNote());
        invoice.setTotalAmount(BigDecimal.ZERO); // Will be calculated below

        // Save invoice first to get the ID
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Create invoice items and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InvoiceItem> invoiceItems = new ArrayList<>();

        for (int i = 0; i < createDto.getItems().size(); i++) {
            InvoiceItemCreateDto itemDto = createDto.getItems().get(i);
            Product product = products.get(i);

            // Use provided unitPrice or product's defaultUnitPrice
            BigDecimal unitPrice = itemDto.getUnitPrice();
            if (unitPrice == null) {
                unitPrice = product.getDefaultUnitPrice();
                if (unitPrice == null) {
                    ErrorResponse error = new ErrorResponse(
                        "UNIT_PRICE_REQUIRED",
                        "Product " + product.getItemCode() + " has no default unit price. Please provide unitPrice."
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            // Calculate amount
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            // Create invoice item
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setInvoice(savedInvoice);
            invoiceItem.setProduct(product);
            invoiceItem.setQuantity(itemDto.getQuantity());
            invoiceItem.setUnitPrice(unitPrice);
            invoiceItem.setAmount(amount);

            invoiceItems.add(invoiceItem);
            totalAmount = totalAmount.add(amount);
        }

        // Update invoice total amount
        savedInvoice.setTotalAmount(totalAmount);
        invoiceRepository.save(savedInvoice);

        // Save all invoice items
        invoiceItemRepository.saveAll(invoiceItems);

        // Convert to DTO
        InvoiceDto invoiceDto = convertToDto(savedInvoice);

        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceDto);
    }

    @GetMapping("/by-customer/{customerId}")
    @Operation(summary = "Get invoices for a customer (newest first)", 
               description = "Returns a list of invoices for the specified customer, ordered by invoice date descending.")
    public ResponseEntity<?> getInvoicesByCustomer(
            @Parameter(description = "Customer ID", required = true) @PathVariable Long customerId) {
        
        // Check if customer exists
        if (!customerRepository.existsById(customerId)) {
            ErrorResponse error = new ErrorResponse(
                "CUSTOMER_NOT_FOUND",
                "Customer with ID " + customerId + " not found"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<Invoice> invoices = invoiceRepository.findByCustomerIdOrderByInvoiceDateDesc(customerId);
        List<InvoiceDto> invoiceDtos = invoices.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(invoiceDtos);
    }

    private InvoiceDto convertToDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCustomerId(invoice.getCustomer().getId());
        dto.setCustomerName(invoice.getCustomer().getNameStd());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setNote(invoice.getNote());

        // Convert invoice items
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());

        List<InvoiceItemDto> itemDtos = items.stream()
                .map(this::convertItemToDto)
                .collect(Collectors.toList());

        dto.setItems(itemDtos);

        return dto;
    }

    private InvoiceItemDto convertItemToDto(InvoiceItem item) {
        InvoiceItemDto dto = new InvoiceItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setItemCode(item.getProduct().getItemCode());
        dto.setDescription(item.getProduct().getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setAmount(item.getAmount());
        return dto;
    }
}

