package com.github.lvpasqualini.ms_paciente.config;

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
                        .title("SaúdePerto - Microsserviço de Pacientes (ms-paciente)")
                        .description("API REST para gerenciamento de pacientes de comunidades vulneráveis e atualização síncrona de status de atendimento.")
                        .version("v1"));
    }
}
