package com.wafula.teza.pricing.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wafula.teza.pricing.application.DistanceProvider;
import com.wafula.teza.pricing.domain.DistanceResult;
import com.wafula.teza.pricing.domain.Location;
import com.wafula.teza.shared.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleDirectionsDistanceProvider implements DistanceProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleDirectionsDistanceProvider.class);

    private final String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleDirectionsDistanceProvider(
            @Value("${teza.google-maps.api-key}") String apiKey,
            @org.springframework.beans.factory.annotation.Autowired(required = false) ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public DistanceResult calculate(Location pickup, Location dropoff) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Google Maps API key is not configured; using fallback distance estimation (straight line).");
            return calculateFallback(pickup, dropoff);
        }

        try {
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                    pickup.latitude(), pickup.longitude(),
                    dropoff.latitude(), dropoff.longitude(),
                    apiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            String status = root.path("status").asText();
            if (!"OK".equals(status)) {
                log.warn("Google Directions API returned non-OK status: {}. Response: {}. Falling back to straight-line estimation.", status, response);
                return calculateFallback(pickup, dropoff);
            }

            JsonNode leg = root.path("routes").get(0).path("legs").get(0);
            double distanceMeters = leg.path("distance").path("value").asDouble();
            double durationSeconds = leg.path("duration").path("value").asDouble();

            double distanceKm = distanceMeters / 1000.0;
            double durationMinutes = durationSeconds / 60.0;

            return new DistanceResult(distanceKm, durationMinutes);
        } catch (Exception e) {
            log.warn("Error calling Google Directions API: {}. Falling back to straight-line estimation.", e.getMessage());
            return calculateFallback(pickup, dropoff);
        }
    }

    private DistanceResult calculateFallback(Location pickup, Location dropoff) {
        // Fallback straight-line distance (Haversine formula) and rough duration estimate
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(dropoff.latitude() - pickup.latitude());
        double dLng = Math.toRadians(dropoff.longitude() - pickup.longitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(pickup.latitude())) * Math.cos(Math.toRadians(dropoff.latitude())) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = earthRadius * c;
        
        // Rough estimate: 30 km/h average speed (2 minutes per km)
        double durationMinutes = distanceKm * 2.0;

        return new DistanceResult(distanceKm, durationMinutes);
    }
}
