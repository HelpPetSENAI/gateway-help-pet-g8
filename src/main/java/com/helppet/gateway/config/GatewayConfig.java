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
     * O nome do bean "ipKeyResolver" e referenciado no application.yml.
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
