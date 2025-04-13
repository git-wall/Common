package org.app.common.action.dos;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@Slf4j
@Getter
public class CmsSeedProvider {

    private final int seed;

    public CmsSeedProvider(@Value("${dos.cms.seed:0}") int configuredSeed) {
        if (configuredSeed != 0) {
            seed = configuredSeed;
            log.info("Using configured CMS seed={}", seed);
        } else {
            // Generate a random 32‑bit seed
            seed = SecureRandomHolder.INSTANCE.nextInt();
            log.warn("No CMS seed configured—generated random seed={}. " +
                    "Add this to your config for stable behavior: dos.cms.seed={}", seed, seed);
        }
    }

    // Lazy‑loaded SecureRandom
    private static class SecureRandomHolder {
        static final SecureRandom INSTANCE = new SecureRandom();
    }
}
