package food.recommender.foodmicroservice;


import food.recommender.gateway.BaseRESTMicroservice;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class FoodRESTAPIService extends BaseRESTMicroservice {
    private static String SERVICE_NAME = "food_service";
    private static int PORT = 8081;
    private static String API_SERVICE_TYPE = "food";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();

        Router router = Router.router(vertx);

        router.get("/" + API_SERVICE_TYPE + "/").handler(this::getFood);

        createAPIServer(router)
                .compose(server -> publishHTTPEndPoint(SERVICE_NAME, HOST, PORT, "/" + API_SERVICE_TYPE))
                .setHandler(event -> {
                    if (event.succeeded()) {
                        startFuture.complete();
                    }
                });
    }

    private void getFood(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "text/plain").end("success!");
    }

    @Override
    protected int getPort() {
        return PORT;
    }
}
