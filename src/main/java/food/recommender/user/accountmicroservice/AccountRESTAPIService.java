package food.recommender.user.accountmicroservice;

import food.recommender.gateway.BaseRESTMicroservice;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.ServiceDiscovery;

public class AccountRESTAPIService extends BaseRESTMicroservice {

    private static String SERVICE_NAME = "account_service";
    private static int PORT = 8081;
    private static String API_SERVICE_TYPE = "accounts";


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        discovery = ServiceDiscovery.create(vertx);
        Router router = Router.router(vertx);

        router.route(HttpMethod.POST, "/"+API_SERVICE_TYPE+"/*").handler(BodyHandler.create());
        router.post("/"+API_SERVICE_TYPE+"/:"+UUID_PARAM);
        createAPIServer(router)
                .compose(server -> publishHTTPEndPoint(SERVICE_NAME, HOST, PORT, "/" + API_SERVICE_TYPE))
                .setHandler(event -> {
                    if (event.succeeded()) {
                        startFuture.complete();
                    }
                });
    }

    @Override
    protected int getPort() {
        return 0;
    }
}
