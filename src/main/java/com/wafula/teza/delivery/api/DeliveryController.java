package com.wafula.teza.delivery.api;

import com.wafula.teza.delivery.api.dto.DeliveryCreateRequest;
import com.wafula.teza.delivery.api.dto.DeliveryOfferResponse;
import com.wafula.teza.delivery.api.dto.DeliveryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryStatusHistoryResponse;
import com.wafula.teza.delivery.api.dto.DeliveryStatusUpdateRequest;
import com.wafula.teza.delivery.api.dto.DeliveryUpdateRequest;
import com.wafula.teza.delivery.api.dto.OfferCreateRequest;
import com.wafula.teza.delivery.api.dto.OfferResponseRequest;
import com.wafula.teza.delivery.api.dto.RiderStatsResponse;
import com.wafula.teza.delivery.application.DeliveryService;
import com.wafula.teza.dispatch.domain.RankedRider;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import com.wafula.teza.shared.api.dto.PagedResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;



/**
 * REST controller for managing deliveries, status updates, status history logs,
 * and matching offers.
 */
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Value("${teza.google-maps.api-key}")
    private String googleMapsApiKey;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse createDelivery(
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody DeliveryCreateRequest request) {
        return deliveryService.createDelivery(currentUserId, request);
    }

    @GetMapping("/{id}")
    public DeliveryResponse getDeliveryById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.getDeliveryById(id, currentUserId, isAdmin);
    }

    @GetMapping("/merchant")
    public List<DeliveryResponse> getDeliveriesForMerchant(@AuthenticationPrincipal UUID currentUserId) {
        return deliveryService.getDeliveriesForMerchant(currentUserId);
    }

    @GetMapping("/customer")
    public List<DeliveryResponse> getDeliveriesForCustomer(@AuthenticationPrincipal UUID currentUserId) {
        return deliveryService.getDeliveriesForCustomer(currentUserId);
    }

    @GetMapping("/rider")
    public List<DeliveryResponse> getDeliveriesForRider(@AuthenticationPrincipal UUID currentUserId) {
        return deliveryService.getDeliveriesForRider(currentUserId);
    }

    @GetMapping("/rider/offers")
    public List<DeliveryOfferResponse> getOffersForRider(@AuthenticationPrincipal UUID currentUserId) {
        return deliveryService.getOffersForRider(currentUserId);
    }

    @GetMapping("/rider/stats")
    public RiderStatsResponse getRiderStats(@AuthenticationPrincipal UUID currentUserId) {
        return deliveryService.getRiderStats(currentUserId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public PagedResponse<DeliveryResponse> getAllDeliveries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return deliveryService.getAllDeliveries(page, size);
    }

    @PutMapping("/{id}")
    public DeliveryResponse updateDelivery(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody DeliveryUpdateRequest request) {
        return deliveryService.updateDelivery(id, currentUserId, request);
    }

    @PutMapping("/{id}/status")
    public DeliveryResponse updateDeliveryStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication,
            @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.updateDeliveryStatus(id, currentUserId, isAdmin, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDelivery(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasSuperAdminRole(authentication);
        deliveryService.deleteDelivery(id, currentUserId, isAdmin);
    }

    @PostMapping("/{id}/offers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryOfferResponse createOffer(
            @PathVariable UUID id,
            @Valid @RequestBody OfferCreateRequest request) {
        return deliveryService.createOffer(id, request);
    }

    @PutMapping("/offers/{offerId}/respond")
    public DeliveryOfferResponse respondToOffer(
            @PathVariable UUID offerId,
            @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody OfferResponseRequest request) {
        return deliveryService.respondToOffer(offerId, currentUserId, request);
    }

    @GetMapping("/offers/{offerId}")
    public DeliveryOfferResponse getOfferById(
            @PathVariable UUID offerId,
            @AuthenticationPrincipal UUID currentUserId) {
        return deliveryService.getOfferById(offerId, currentUserId);
    }

    @GetMapping("/{id}/offers")
    public List<DeliveryOfferResponse> getOffersForDelivery(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.getOffersForDelivery(id, currentUserId, isAdmin);
    }

    @GetMapping("/{id}/history")
    public List<DeliveryStatusHistoryResponse> getStatusHistory(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.getStatusHistory(id, currentUserId, isAdmin);
    }

    @GetMapping("/{id}/matching-riders")
    public List<RankedRider> getMatchingRiders(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID currentUserId,
            Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return deliveryService.findMatchingRiders(id, currentUserId, isAdmin);
    }

    @GetMapping("/places/autocomplete")
    public String getPlacesAutocomplete(@RequestParam("input") String input) {
        try {
            String encodedInput = URLEncoder.encode(input, StandardCharsets.UTF_8);
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?"
                    + "input=" + encodedInput
                    + "&components=country:ke"
                    + "&location=-1.286389,36.817223"
                    + "&radius=40000"
                    + "&strictbounds=true"
                    + "&key=" + googleMapsApiKey;
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "{\"predictions\":[],\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/places/details")
    public String getPlaceDetails(@RequestParam("placeId") String placeId) {
        try {
            String encodedPlaceId = URLEncoder.encode(placeId, StandardCharsets.UTF_8);
            String url = "https://maps.googleapis.com/maps/api/place/details/json?"
                    + "place_id=" + encodedPlaceId
                    + "&fields=geometry"
                    + "&key=" + googleMapsApiKey;
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "{\"result\":{},\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/places/reverse-geocode")
    public String reverseGeocode(@RequestParam("lat") double lat, @RequestParam("lng") double lng) {
        try {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?"
                    + "latlng=" + lat + "," + lng
                    + "&key=" + googleMapsApiKey;
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "{\"results\":[],\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private boolean hasAdminRole(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                    grantedAuthority.getAuthority().equals("ROLE_SUPPORT_ADMIN"));
    }

    private boolean hasSuperAdminRole(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public ResponseEntity<byte[]> exportDeliveriesToCsv() {
        List<DeliveryResponse> deliveries = deliveryService.getAllDeliveries();
        
        StringBuilder csv = new StringBuilder();
        csv.append("Delivery ID,Merchant ID,Customer ID,Rider ID,Status,Pickup Address,Dropoff Address,Delivery Fee (KES)\n");
        
        for (DeliveryResponse d : deliveries) {
            csv.append(d.id() != null ? escapeCsvField(d.id().toString()) : "").append(",")
               .append(d.merchantId() != null ? escapeCsvField(d.merchantId().toString()) : "").append(",")
               .append(d.customerId() != null ? escapeCsvField(d.customerId().toString()) : "").append(",")
               .append(d.riderId() != null ? escapeCsvField(d.riderId().toString()) : "").append(",")
               .append(escapeCsvField(d.status().name())).append(",")
               .append(escapeCsvField(d.pickupAddress())).append(",")
               .append(escapeCsvField(d.dropoffAddress())).append(",")
               .append(d.deliveryFee()).append("\n");
        }
        
        byte[] data = csv.toString().getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "deliveries.csv");
        
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
    
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPPORT_ADMIN')")
    public ResponseEntity<byte[]> exportDeliveriesToExcel() {
        List<DeliveryResponse> deliveries = deliveryService.getAllDeliveries();
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Deliveries");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            
            Row headerRow = sheet.createRow(0);
            String[] headersList = { "Delivery ID", "Merchant ID", "Customer ID", "Rider ID", "Status", "Pickup Address", "Dropoff Address", "Delivery Fee (KES)" };
            for (int i = 0; i < headersList.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headersList[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowIdx = 1;
            for (DeliveryResponse d : deliveries) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.id() != null ? d.id().toString() : "");
                row.createCell(1).setCellValue(d.merchantId() != null ? d.merchantId().toString() : "");
                row.createCell(2).setCellValue(d.customerId() != null ? d.customerId().toString() : "");
                row.createCell(3).setCellValue(d.riderId() != null ? d.riderId().toString() : "");
                row.createCell(4).setCellValue(d.status().name());
                row.createCell(5).setCellValue(d.pickupAddress());
                row.createCell(6).setCellValue(d.dropoffAddress());
                
                Cell feeCell = row.createCell(7);
                feeCell.setCellValue(d.deliveryFee() != null ? d.deliveryFee().doubleValue() : 0.0);
            }
            
            for (int i = 0; i < headersList.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            byte[] data = out.toByteArray();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "deliveries.xlsx");
            
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
}
