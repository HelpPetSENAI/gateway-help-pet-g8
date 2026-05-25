package com.helppet.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de Health Check Agregado.
 * Verifica a saúde do Gateway e dos 5 microsserviços:
 * - G1: Auth + Users
 * - G2: Pets
 * - G3: Adoption
 * - G4: Chat
 * - G5: Notifications
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final WebClient webClient;

    @Value("${G1_URL:http://localhost:8081}")
    private String g1Url;

    @Value("${G2_URL:http://localhost:8082}")
    private String g2Url;

    @Value("${G3_URL:http://localhost:8083}")
    private String g3Url;

    @Value("${G4_URL:http://localhost:8084}")
    private String g4Url;

    @Value("${G5_URL:http://localhost:8085}")
    private String g5Url;

    @Value("${health.monitor.g1-auth-users:true}")
    private boolean monitorG1;

    @Value("${health.monitor.g2-pets:true}")
    private boolean monitorG2;

    @Value("${health.monitor.g3-adoption:true}")
    private boolean monitorG3;

    @Value("${health.monitor.g4-chat:true}")
    private boolean monitorG4;

    @Value("${health.monitor.g5-notifications:true}")
    private boolean monitorG5;

    public HealthController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getHealth() {
        Mono<String> g1Health = checkService(monitorG1, g1Url, "G1 - Auth & Users");
        Mono<String> g2Health = checkService(monitorG2, g2Url, "G2 - Pets");
        Mono<String> g3Health = checkService(monitorG3, g3Url, "G3 - Adoption");
        Mono<String> g4Health = checkService(monitorG4, g4Url, "G4 - Chat");
        Mono<String> g5Health = checkService(monitorG5, g5Url, "G5 - Notifications");

        return Mono.zip(g1Health, g2Health, g3Health, g4Health, g5Health)
                .map(tuple -> {
                    Map<String, Object> services = new LinkedHashMap<>();
                    services.put("g1_auth_users", tuple.getT1());
                    services.put("g2_pets", tuple.getT2());
                    services.put("g3_adoption", tuple.getT3());
                    services.put("g4_chat", tuple.getT4());
                    services.put("g5_notifications", tuple.getT5());

                    // Verificar se os serviços monitorados obrigatórios estão UP
                    boolean allRequired = true;
                    if (monitorG1 && !"UP".equals(tuple.getT1())) allRequired = false;
                    if (monitorG2 && !"UP".equals(tuple.getT2())) allRequired = false;
                    if (monitorG3 && !"UP".equals(tuple.getT3())) allRequired = false;
                    if (monitorG4 && !"UP".equals(tuple.getT4())) allRequired = false;
                    if (monitorG5 && !"UP".equals(tuple.getT5())) allRequired = false;

                    Map<String, Object> response = new HashMap<>();
                    response.put("gateway_status", "UP");
                    response.put("overall_status", allRequired ? "UP" : "DEGRADED");
                    response.put("services", services);
                    response.put("timestamp", System.currentTimeMillis());

                    HttpStatus status = allRequired ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                    return ResponseEntity.status(status).body(response);
                });
    }

    /**
     * Verifica a saúde de um microsserviço
     */
    private Mono<String> checkService(boolean monitor, String serviceUrl, String serviceName) {
        if (!monitor) {
            return Mono.just("NOT_CONFIGURED");
        }

        return webClient.get()
                .uri(serviceUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(result -> String.valueOf(result.getOrDefault("status", "DOWN")))
                .timeout(Duration.ofSeconds(2))
                .onErrorReturn("DOWN")
                .doOnError(error -> System.err.println("Health check failed for " + serviceName + ": " + error.getMessage()));
    }
}