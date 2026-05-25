package com.helppet.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Access log centralizado para todas as requisicoes que passam pelo Gateway.
 */
@Component
public class AccessLogGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AccessLogGlobalFilter.class);
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String ATTRIBUTE_REQUEST_ID = "accessLogRequestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        long start = System.currentTimeMillis();

        String requestId = exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;
        exchange.getAttributes().put(ATTRIBUTE_REQUEST_ID, finalRequestId);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long durationMs = System.currentTimeMillis() - start;
                int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeId = route != null ? route.getId() : "no-route";
                String method = exchange.getRequest().getMethod() != null
                    ? exchange.getRequest().getMethod().name()
                            : "UNKNOWN";
                String path = exchange.getRequest().getURI().getPath();
                // Usa sempre o IP real da conexão TCP para evitar log injection via X-Forwarded-For
                String clientIp = exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown";

                    log.info("reqId={} method={} path={} route={} status={} durationMs={} clientIp={}",
                            finalRequestId,
                            method,
                            path,
                            routeId,
                            statusCode,
                            durationMs,
                            clientIp);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
