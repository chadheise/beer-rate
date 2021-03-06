package com.manorllc.beerRate;

import com.google.inject.AbstractModule;
import com.manorllc.beerRate.config.CompositeConfig;
import com.manorllc.beerRate.config.Config;
import com.manorllc.beerRate.config.ConfigKeys;
import com.manorllc.beerRate.config.EnvironmentConfig;
import com.manorllc.beerRate.db.Database;
import com.manorllc.beerRate.db.DatabaseQueries;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.templ.TemplateEngine;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

public class AppInjector extends AbstractModule {

    private static final Config CONFIG = new CompositeConfig(new EnvironmentConfig());

    private static final int DEFAULT_PORT = 80; // 8080;
    private static final int PORT = CONFIG.getInteger(ConfigKeys.PORT).orElse(DEFAULT_PORT);

    private static final Database DB = new Database();
    private static final DatabaseQueries QUERIES = new DatabaseQueries(DB);
    private static final TemplateEngine TEMPLATE_ENGINE = ThymeleafTemplateEngine.create();

    private static final ApiHandlers API_HANDLERS = new ApiHandlers(DB, QUERIES);
    private static final UiHandlers UI_HANDLERS = new UiHandlers(TEMPLATE_ENGINE, DB, QUERIES);

    @Override
    protected void configure() {
        bind(ApiHandlers.class).toInstance(API_HANDLERS);
        bind(AbstractVerticle.class).toInstance(new Server(PORT, API_HANDLERS, UI_HANDLERS));
        bind(Database.class).toInstance(DB);
    }

}
