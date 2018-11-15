package food.recommender.gateway;

import food.recommender.user.accountmicroservice.EventBusProxy.UserAccountService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.serviceproxy.ServiceBinder;

public abstract class BaseRESTMicroservice extends AbstractVerticle {


    protected ServiceDiscovery discovery;
    protected static String UUID_PARAM = "uuid";
    protected static String HOST = "localhost";

    protected Future<Void> publishEventBusProxy(String name, String address, UserAccountService service){
        Future<Void> future = Future.future();

        new ServiceBinder(vertx)
                .setAddress(address)
                .register(UserAccountService.class, service);
        Record record = EventBusService.createRecord(name,address,UserAccountService.class);
        discovery.publish(record, ar->{
            if(ar.succeeded()){
                Record publishedRecord = ar.result();
                System.out.println("Record name is: " + publishedRecord.getName());
                future.complete();
            }
            else{
                System.out.println("service failure");
                discovery.close();

            }
        });
        return future;
    }

    protected Future<Void> publishHTTPEndPoint(String name, String host, int port, String apiPath) {
        Record record = HttpEndpoint.createRecord(name, host, port, apiPath);
        Future<Void> future = Future.future();

        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                // publication succeeded
                Record publishedRecord = ar.result();
                System.out.println("Record name is: " + publishedRecord.getName());
                future.complete();
            } else {
                // publication failed
                System.out.println("service failure");
                discovery.close();

            }
        });
        return future;
    }

    protected Future<Void> createAPIServer(Router router) {
        Future<HttpServer> serverFuture = Future.future();
        vertx.createHttpServer().
                requestHandler(router::accept).
                listen(getPort(), res -> {
                    if (res.succeeded()) {
                        serverFuture.complete();

                    }
                });
        return serverFuture.map(server -> null);
    }

    protected abstract int getPort();
}
