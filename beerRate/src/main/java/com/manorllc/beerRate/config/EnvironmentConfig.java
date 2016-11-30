package com.manorllc.beerRate.config;

import java.util.Optional;

/**
 * Reads configuration from environment variables
 *
 */
public class EnvironmentConfig extends AbstractConfig {

    @Override
    public Optional<String> get(final String key) {
        String envKey = key.toUpperCase().replace('.', '_');
        return Optional.ofNullable(System.getenv(envKey));
    }

}
