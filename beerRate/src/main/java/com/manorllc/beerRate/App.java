package com.manorllc.beerRate;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Main entry point for the application.
 */
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting application");
        Injector injector = Guice.createInjector(new AppInjector());

        AbstractVerticle server = injector.getInstance(AbstractVerticle.class);
        Vertx.vertx().deployVerticle(server);
    }
}
