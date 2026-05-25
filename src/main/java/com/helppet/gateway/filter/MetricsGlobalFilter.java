package com.helppet.gateway.filter;

import com.helppet.gateway.service.MetricsService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MetricsGlobalFilter implements GlobalFilter, Ordered {

    private final MetricsService metricsService;

    public MetricsGlobalFilter(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Increment request count when a request hits the gateway
        metricsService.incrementRequestCount();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Increment response count when the response is going back
            metricsService.incrementResponseCount();
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
