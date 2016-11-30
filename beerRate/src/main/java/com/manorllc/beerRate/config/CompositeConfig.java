package com.manorllc.beerRate.config;

import java.util.Optional;

/**
 * Uses a chain of responsibility to check underlying configs for values.
 *
 */
public class CompositeConfig extends AbstractConfig {

    private final Config[] configs;

    public CompositeConfig(final Config... configs) {
        this.configs = configs;
    }

    @Override
    public Optional<String> get(final String key) {
        for (Config config : configs) {
            if (config.get(key).isPresent()) {
                return config.get(key);
            }
        }
        return Optional.empty();
    }

}
