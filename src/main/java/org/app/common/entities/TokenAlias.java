package org.app.common.entities;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.app.common.fluent.ChainSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@ChainSetter(fluent = true, chain = true)
public class TokenAlias {
    @JsonAlias({"access_token", "accessToken", "token", "accesstoken", "ACCESSTOKEN"})
    private String token;
    @JsonAlias({"refresh_token", "refresh", "refreshToken", "refreshtoken", "REFRESHTOKEN"})
    private String refreshToken;
    @JsonAlias({"expires_in", "expiresIn", "expiresin", "EXPIRESIN"})
    private String expiresIn;
}
