package food.recommender.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

public abstract class BaseRESTMicroservice extends AbstractVerticle {


    private ServiceDiscovery discovery;

    protected static String HOST = "localhost";


    @Override
    public void start() throws Exception {
        discovery = ServiceDiscovery.create(vertx);

    }

    protected Future<Void> publishHTTPEndPoint(String name, String host, int port, String apiPath) {
        Record record = HttpEndpoint.createRecord(name, host, port, apiPath);
        Future<Void> future = Future.future();

        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                // publication succeeded
                Record publishedRecord = ar.result();
                System.out.println("record name is: " + publishedRecord.getName());
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
                listen(getPort(), res->{
                    if(res.succeeded()){
                        serverFuture.complete();

                    }
                });
        return serverFuture.map(server->null);
    }

    protected abstract int getPort();
}
