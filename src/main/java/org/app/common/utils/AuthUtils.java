package org.app.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

/**
 * Get info client from token
 */
public class AuthUtils {

    private AuthUtils() {
    }

    public static String getCurrentUsername() {
        Authentication auth = getAuthHolder();
        return Optional.ofNullable(auth.getPrincipal())
                .map(AuthUtils::getUserName)
                .orElse(null);
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
        Object principal = getAuthHolder().getPrincipal();
        return clazz.isInstance(principal) ? clazz.cast(principal) : null;
    }

    private static String getPrincipal(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .map(a -> getUserName(a.getPrincipal()))
                .orElse(null);
    }

    @SuppressWarnings({"unchecked"})
    public static String getUserName(Object principal) {
        if (principal instanceof User) {
            return ((User) principal).getUsername();
        }

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        if (principal instanceof OAuth2AuthenticationToken) {
            if (principal instanceof HashMap) {
                HashMap<String, Object> principalMap = (HashMap<String, Object>) principal;
                if (principalMap.containsKey("username")) {
                    return principalMap.get("username").toString();
                }
            }
        }

        if (principal instanceof String) {
            return principal.toString();
        }

        return null;
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
        return Optional.ofNullable(clientRegistrationRepository)
                .map(repo -> repo.findByRegistrationId(registrationId))
                .map(ClientRegistration::getClientId)
                .orElse(null);
    }

    public static Authentication getAuthHolder() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .orElse(null);
    }
}
