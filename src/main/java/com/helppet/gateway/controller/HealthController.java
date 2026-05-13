package com.helppet.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint de Health Check Agregado.
 * Verifica a saude do Gateway e faz proxy para verificar se o Microservico HelpPet esta de pe.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final WebClient webClient;

    @Value("${IDENTITY_SECURITY_URL:http://localhost:8081}")
    private String identityServiceUrl;

    @Value("${PET_MANAGEMENT_URL:http://localhost:8082}")
    private String petServiceUrl;

    @Value("${LOST_FOUND_PETS_URL:http://localhost:8083}")
    private String lostFoundServiceUrl;

    @Value("${LOCATION_DISTANCE_URL:http://localhost:8084}")
    private String locationServiceUrl;

    @Value("${ADOPTION_PROCESS_URL:http://localhost:8085}")
    private String adoptionServiceUrl;

    @Value("${COMMUNICATION_CHAT_URL:http://localhost:8086}")
    private String chatServiceUrl;

    @Value("${SYSTEM_ALERTS_URL:http://localhost:8087}")
    private String alertServiceUrl;

    @Value("${health.monitor.identity-security:true}")
    private boolean monitorIdentity;

    @Value("${health.monitor.pet-management:true}")
    private boolean monitorPet;

    @Value("${health.monitor.lost-found:false}")
    private boolean monitorLostFound;

    @Value("${health.monitor.location-distance:false}")
    private boolean monitorLocation;

    @Value("${health.monitor.adoption-process:false}")
    private boolean monitorAdoption;

    @Value("${health.monitor.communication-chat:false}")
    private boolean monitorChat;

    @Value("${health.monitor.system-alerts:false}")
    private boolean monitorAlerts;

    public HealthController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getHealth() {
        Mono<String> identityHealth = checkService(monitorIdentity, identityServiceUrl);
        Mono<String> petHealth = checkService(monitorPet, petServiceUrl);
        Mono<String> lostFoundHealth = checkService(monitorLostFound, lostFoundServiceUrl);
        Mono<String> locationHealth = checkService(monitorLocation, locationServiceUrl);
        Mono<String> adoptionHealth = checkService(monitorAdoption, adoptionServiceUrl);
        Mono<String> chatHealth = checkService(monitorChat, chatServiceUrl);
        Mono<String> alertHealth = checkService(monitorAlerts, alertServiceUrl);

        return Mono.zip(
                        identityHealth,
                        petHealth,
                        lostFoundHealth,
                        locationHealth,
                        adoptionHealth,
                        chatHealth,
                        alertHealth
                )
                .map(tuple -> {
                    Map<String, Object> services = new LinkedHashMap<>();
                    services.put("identity_security", tuple.getT1());
                    services.put("pet_management", tuple.getT2());
                    services.put("lost_found", tuple.getT3());
                    services.put("location_distance", tuple.getT4());
                    services.put("adoption_process", tuple.getT5());
                    services.put("communication_chat", tuple.getT6());
                    services.put("system_alerts", tuple.getT7());

                    boolean requiredUp = true;
                    if (monitorIdentity && !"UP".equals(tuple.getT1())) {
                        requiredUp = false;
                    }
                    if (monitorPet && !"UP".equals(tuple.getT2())) {
                        requiredUp = false;
                    }
                    if (monitorLostFound && !"UP".equals(tuple.getT3())) {
                        requiredUp = false;
                    }
                    if (monitorLocation && !"UP".equals(tuple.getT4())) {
                        requiredUp = false;
                    }
                    if (monitorAdoption && !"UP".equals(tuple.getT5())) {
                        requiredUp = false;
                    }
                    if (monitorChat && !"UP".equals(tuple.getT6())) {
                        requiredUp = false;
                    }
                    if (monitorAlerts && !"UP".equals(tuple.getT7())) {
                        requiredUp = false;
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("gateway_status", "UP");
                    response.put("overall_status", requiredUp ? "UP" : "DEGRADED");
                    response.put("services", services);
                    response.put("timestamp", System.currentTimeMillis());

                    if (requiredUp) {
                        return ResponseEntity.ok(response);
                    }

                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
                });
    }

    private Mono<String> checkService(boolean monitor, String serviceBaseUrl) {
        if (!monitor) {
            return Mono.just("NOT_CONFIGURED");
        }
        return webClient.get()
                .uri(serviceBaseUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .map((Map<String, Object> result) -> String.valueOf(result.getOrDefault("status", "DOWN")))
                .timeout(Duration.ofSeconds(2))
                .onErrorReturn("DOWN");
    }
}