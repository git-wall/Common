package org.app.common.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(title = "${spring.application.name}", description = "${spring.application.name}", version = "${spring.application.version}"),
        security = @SecurityRequirement(name = "oauth2_bearer"),
        servers = {@Server(url = "${server.servlet.context-path}", description = "Default Server URL")})
@SecurityScheme(
        name = "oauth2_bearer", type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "${springdoc.oauth.authorization-url}",
                        tokenUrl = "${springdoc.oauth.token-url}",
                        scopes = {
                                @OAuthScope(name = "openid", description = "openid")
                        })
        ))
public class SwaggerConfig {
}
