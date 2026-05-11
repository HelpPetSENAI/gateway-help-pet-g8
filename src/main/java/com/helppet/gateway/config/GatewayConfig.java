package com.helppet.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Configuracoes gerais do Gateway.
 *
 * IpKeyResolver: resolve a chave do Rate Limiter pelo IP do cliente.
 * Cada IP tem sua propria cota de requisicoes por segundo.
 */
@Configuration
public class GatewayConfig {

    /**
     * Resolve a chave do Rate Limiter pelo IP do cliente.
     * Considera ambientes de nuvem (Load Balancer, Proxy) checando X-Forwarded-For antes do remoto.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                // Em caso de multiplos IPs, pega o primeiro (o original)
                String ip = xForwardedFor.split(",")[0].trim();
                return Mono.just(ip);
            }
            
            return Mono.just(
                    Objects.requireNonNull(
                            exchange.getRequest().getRemoteAddress(),
                            "RemoteAddress nao pode ser nulo"
                    ).getAddress().getHostAddress()
            );
        };
    }
}
