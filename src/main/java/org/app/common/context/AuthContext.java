package org.app.common.context;

import org.app.common.utils.RequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.util.Collection;
import java.util.Optional;
/**
 * Get info client from token
 * */
public class AuthContext {

    private AuthContext() {
    }

    public static String getUserName() {
        return getAuthHolder().getName();
    }

    public static Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthHolder().getAuthorities();
    }

    public static Object getCredentials() {
        return getAuthHolder().getCredentials();
    }

    public static Object getDetails() {
        return getAuthHolder().getDetails();
    }

    public static <T> T getPrincipal(Class<T> clazz) {
        return clazz.cast(getAuthHolder().getPrincipal());
    }

    private static String getPrincipal(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .map(a -> getUserName(a.getPrincipal()))
                .orElse(null);
    }

    public static String getUserName(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        if (principal instanceof String) {
            return principal.toString();
        }

        return null;
    }

    public static String getDetails(Authentication auth){
        return Optional.ofNullable(auth)
                .map(Authentication::getDetails)
                .filter(authDetails -> authDetails instanceof WebAuthenticationDetails)
                .map(authDetails -> RequestUtils.getRemoteAddress((WebAuthenticationDetails) authDetails))
                .orElse(null);
    }

    public static String getClientId(OAuth2AuthenticationToken auth) {
        return Optional.ofNullable(auth)
                .map(OAuth2AuthenticationToken::getAuthorizedClientRegistrationId)
                .orElse(null);
    }

    public static String getClientId(OAuth2AuthorizedClient authorizedClient) {
        return Optional.ofNullable(authorizedClient)
                .map(OAuth2AuthorizedClient::getClientRegistration)
                .map(ClientRegistration::getClientId)
                .orElse(null);
    }

    public static String getClientId(ClientRegistrationRepository clientRegistrationRepository, String registrationId) {
        return Optional.ofNullable(clientRegistrationRepository.findByRegistrationId(registrationId))
                .map(ClientRegistration::getClientId)
                .orElse(null);
    }

    private static Authentication getAuthHolder() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .orElse(null);
    }
}
