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
     * Resolve a chave do Rate Limiter pelo IP real da conexão TCP.
     * Não usa X-Forwarded-For pois o header pode ser forjado por qualquer cliente,
     * permitindo bypass do rate limiter. Em produção com load balancer confiável,
     * configure um RemoteAddressResolver com trusted proxies explícitos.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(
                        exchange.getRequest().getRemoteAddress(),
                        "RemoteAddress nao pode ser nulo"
                ).getAddress().getHostAddress()
        );
    }
}
