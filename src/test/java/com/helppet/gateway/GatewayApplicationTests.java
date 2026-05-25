package com.helppet.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-must-be-at-least-64-characters-long-for-hmac-sha",
        "jwt.expiration=86400000",
        "api.security.internal-token=test-internal-token-32-chars-here",
        "cors.allowed-origins=http://localhost:3000",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "management.health.redis.enabled=false",
        "spring.cloud.gateway.default-filters[0].name=RequestRateLimiter",
        "spring.cloud.gateway.default-filters[0].args.redis-rate-limiter.replenishRate=20",
        "spring.cloud.gateway.default-filters[0].args.redis-rate-limiter.burstCapacity=40",
        "spring.cloud.gateway.default-filters[0].args.redis-rate-limiter.requestedTokens=1",
        "spring.cloud.gateway.default-filters[0].args.key-resolver=#{@ipKeyResolver}",
        "G1_URL=http://localhost:8081",
        "G2_URL=http://localhost:8082",
        "G3_URL=http://localhost:8083",
        "G4_URL=http://localhost:8084",
        "G5_URL=http://localhost:8085"
})
class GatewayApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que o contexto Spring sobe sem erros de configuracao
    }

}
