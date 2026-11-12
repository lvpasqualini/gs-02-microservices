package com.github.lvpasqualini.ms_triagem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SaúdePerto - Microsserviço de Triagem Clínica (ms-triagem)")
                        .description("API REST para registro de triagens clínicas, classificação automática de risco e comunicação síncrona resiliente com ms-paciente.")
                        .version("v1"));
    }
}
