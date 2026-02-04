package kr.co.softice.mes.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) Configuration
 *
 * @author Moon Myung-seop
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI soIceMesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SoIce MES API")
                        .description("Base MES Platform - REST API Documentation")
                        .version("v0.1.0-SNAPSHOT")
                        .contact(new Contact()
                                .name("Moon Myung-seop")
                                .email("msmoon@softice.co.kr")
                                .url("http://www.softice.co.kr"))
                        .license(new License()
                                .name("Proprietary")
                                .url("http://www.softice.co.kr")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server (Without context path)")
                ));
    }
}
