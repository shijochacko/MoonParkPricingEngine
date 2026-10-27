package no.moonpark.pricing.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import no.moonpark.pricing.security.filter.ApiKeyAuthenticationFilter;

/**
 * OpenAPI 3 description shown in Swagger UI. Both login types can be tried from the UI.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
    public static final String API_KEY_AUTH = "apiKeyAuth";

    @Bean
    OpenAPI moonParkOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MoonPark Pricing API")
                        .version("v1")
                        .description("""
                                Calculates parking fees for MoonPark rate zones (M1, M2, M3, ...).

                                * People log in with `POST /api/v1/auth/login` and send `Authorization: Bearer <token>`.
                                * Third-party systems send `X-API-Key: <key>`.
                                * Every caller is rate limited. See the `RateLimit-*` and `Retry-After` headers.
                                """)
                        .contact(new Contact().name("MoonPark Platform Team").email("platform@moonpark.no")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes(API_KEY_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
                                .name(ApiKeyAuthenticationFilter.API_KEY_HEADER)))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_AUTH));
    }
}
