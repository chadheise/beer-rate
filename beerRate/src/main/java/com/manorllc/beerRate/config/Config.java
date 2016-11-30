package com.manorllc.beerRate.config;

import java.util.Optional;

public interface Config {

    Optional<String> get(final String key);

    Optional<Integer> getInteger(final String key);

}
