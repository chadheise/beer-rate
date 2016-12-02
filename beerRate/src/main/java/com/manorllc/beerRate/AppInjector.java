package com.manorllc.beerRate;

import com.google.inject.AbstractModule;
import com.manorllc.beerRate.config.CompositeConfig;
import com.manorllc.beerRate.config.Config;
import com.manorllc.beerRate.config.ConfigKeys;
import com.manorllc.beerRate.config.EnvironmentConfig;
import com.manorllc.beerRate.db.BeerRatingDatabase;
import com.manorllc.beerRate.db.InMemoryBeerRatingDatabase;

import io.vertx.core.AbstractVerticle;

public class AppInjector extends AbstractModule {

    private static final Config CONFIG = new CompositeConfig(new EnvironmentConfig());

    private static final int DEFAULT_PORT = 80; // 8080;
    private static final int PORT = CONFIG.getInteger(ConfigKeys.PORT).orElse(DEFAULT_PORT);

    private static final BeerRatingDatabase DB = new InMemoryBeerRatingDatabase();
    private static final Handlers HANDLERS = new Handlers(DB);

    @Override
    protected void configure() {
        // bind(AbstractVerticle.class).to(Server.class);
        bind(Handlers.class).toInstance(HANDLERS);
        bind(AbstractVerticle.class).toInstance(new Server(PORT, HANDLERS));
    }

}
