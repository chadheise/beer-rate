package com.manorllc.beerRate;

import com.google.inject.AbstractModule;
import com.manorllc.beerRate.config.CompositeConfig;
import com.manorllc.beerRate.config.Config;
import com.manorllc.beerRate.config.ConfigKeys;
import com.manorllc.beerRate.config.EnvironmentConfig;

import io.vertx.core.AbstractVerticle;

public class AppInjector extends AbstractModule {

    private final static Config CONFIG = new CompositeConfig(new EnvironmentConfig());

    private final static int DEFAULT_PORT = 8080;
    private final static int PORT = CONFIG.getInteger(ConfigKeys.PORT).orElse(DEFAULT_PORT);

    @Override
    protected void configure() {
        // bind(AbstractVerticle.class).to(Server.class);
        bind(AbstractVerticle.class).toInstance(new Server(PORT));
    }

}
