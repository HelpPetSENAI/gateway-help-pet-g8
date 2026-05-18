package com.helppet.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuração de rotas públicas do gateway.
 * Lê diretamente do application.yml sem necessidade de @Value.
 */
@Component
@ConfigurationProperties(prefix = "api.security")
public class SecurityPathsConfig {

    private List<String> publicPaths = new ArrayList<>();
    private List<String> publicPostPaths = new ArrayList<>();

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public List<String> getPublicPostPaths() {
        return publicPostPaths;
    }

    public void setPublicPostPaths(List<String> publicPostPaths) {
        this.publicPostPaths = publicPostPaths;
    }
}
