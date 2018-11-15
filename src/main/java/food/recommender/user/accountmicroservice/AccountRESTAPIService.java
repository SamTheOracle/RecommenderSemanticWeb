package food.recommender.user.accountmicroservice;

import food.recommender.ConsumerAddresses;
import food.recommender.gateway.BaseRESTMicroservice;
import food.recommender.user.accountmicroservice.EventBusProxy.UserAccountService;
import food.recommender.user.accountmicroservice.EventBusProxy.UserAccountServiceImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;

public class AccountRESTAPIService extends BaseRESTMicroservice {

    private static String SERVICE_NAME = "accounts_service";
    private static int PORT = 8082;
    private static String API_SERVICE_TYPE = "accounts";


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        discovery = ServiceDiscovery.create(vertx);
        Router router = Router.router(vertx);

        router.route(HttpMethod.POST, "/" + API_SERVICE_TYPE + "/*").handler(BodyHandler.create());
        router.post("/" + API_SERVICE_TYPE ).handler(this::userPost);
        createAPIServer(router)
                .compose(server -> publishHTTPEndPoint(SERVICE_NAME, HOST, PORT, "/" + API_SERVICE_TYPE))
                .compose(httpEndpoint -> publishEventBusProxy(SERVICE_NAME, ConsumerAddresses.ACCOUNT_DB_PROXY,
                        new UserAccountServiceImpl(vertx)))
                .setHandler(event -> {
                    if (event.succeeded()) {
                        startFuture.complete();

                    }
                });
    }

    private void userPost(RoutingContext routingContext) {
        JsonObject user = routingContext.getBody().toJsonObject();
        System.out.println("user post");

        EventBusService.getProxy(discovery, UserAccountService.class, ar -> {
            if (ar.succeeded()) {
                UserAccountService service = ar.result();
                service.save("users", user, res2 -> {
                    if (res2.succeeded()) {
                        // done
                        System.out.println("Successfully created resource: " + user);
                        routingContext.response().putHeader("content-type", "application/json")
                                .setStatusCode(201)
                                .end();
                    }
                    else{

                    }
                });
// Dont' forget to release the service
                ServiceDiscovery.releaseServiceObject(discovery, service);
            }
        });

    }

    @Override
    protected int getPort() {
        return PORT;
    }
}
