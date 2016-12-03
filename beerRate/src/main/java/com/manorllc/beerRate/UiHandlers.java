package com.manorllc.beerRate;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.TemplateEngine;

public class UiHandlers {

    private final TemplateEngine templateEngine;

    public UiHandlers(final TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void mainUi(final RoutingContext routingContext) {
        String beerName = routingContext.request().getParam(HttpConstants.PARAM_BEER);
        // Add beer directly for easier access in template
        routingContext.put("beer", beerName);

        // and now delegate to the engine to render it.
        templateEngine.render(routingContext, "templates/mainUi.html", res -> {
            if (res.succeeded()) {
                routingContext.response().end(res.result());
            } else {
                System.out.println(res.cause());
                routingContext.fail(res.cause());
            }
        });
    }

}
