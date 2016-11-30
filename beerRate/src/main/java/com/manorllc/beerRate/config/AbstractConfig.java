package com.manorllc.beerRate.config;

import java.util.Optional;

/**
 * Convenience Config implementation that handles transforming Strings to other
 * types.
 *
 */
public abstract class AbstractConfig implements Config {

    @Override
    public final Optional<Integer> getInteger(String key) {
        if (get(key).isPresent()) {
            try {
                return Optional.of(Integer.parseInt(get(key).get()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

}
