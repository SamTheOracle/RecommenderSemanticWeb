package food.recommender.gateway;


import food.recommender.foodmicroservice.FoodRESTAPIService;
import food.recommender.user.accountmicroservice.AccountRESTAPIService;
import food.recommender.user.accountmicroservice.EventBusProxy.UserAccountService;
import food.recommender.user.accountmicroservice.EventBusProxy.UserAccountServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.VCARD;

import java.io.*;
import java.util.List;
import java.util.Optional;

public class WebServerLauncher extends AbstractVerticle {
    public static String GATEWAY_NAME = "recommender";

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServerLauncher());
    }

    @Override
    public void start() throws Exception {
// some definitions
        vertx.deployVerticle(new FoodRESTAPIService());
        vertx.deployVerticle(new AccountRESTAPIService());





        Router router = Router.router(vertx);
        router.route("/").handler(routingContext -> {
            System.out.println("LISTENING");
            HttpServerResponse response = routingContext.response();
            response.setStatusCode(200).putHeader("content-type", "text/plain").end("success");
//            request.next(); //next route will handle it
        });
        router.route(HttpMethod.POST, "/" + GATEWAY_NAME + "/*").handler(BodyHandler.create());

        router.route(HttpMethod.PUT, "/" + GATEWAY_NAME + "/*").handler(BodyHandler.create());

        router.route("/" + GATEWAY_NAME + "/*").handler(this::dispatchRequestsToServices);

        //creare una route per una richiesta specifica del client e con i future comporre i risultati :)
        vertx.createHttpServer().requestHandler(router::accept).listen(8080, event -> {
            if (event.succeeded()) {
                System.out.println("GATEWAY DEPLOYED");
            }
        });

    }

    private void dispatchRequestsToServices(RoutingContext routingContext) {
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);

        discovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE), ar -> {
            if (ar.succeeded()) {
                List<Record> recordList = ar.result();
                String path = routingContext.request().uri();
                String serviceName = path.split("/")[2] + "_service";
                System.out.println(serviceName);
                Optional<Record> osadqn = Optional.empty();
                recordList.forEach(record -> {
                    String name = record.getName();
                    if (record.getName().equals(serviceName)) {
                        Optional<Record> osadn = Optional.of(record);
                    }
                });
                Optional<Record> recordToBeFetched = recordList.stream()
                        .filter(record -> record.getName().equals(serviceName))
                        .findAny();
                if (recordToBeFetched.isPresent()) {
                    System.out.println("record is fetched " + recordToBeFetched.get().getName());
                    Record serviceRecord = recordToBeFetched.get();
                    ServiceReference reference = discovery.getReference(serviceRecord);
                    HttpClient client = reference.getAs(HttpClient.class); //new client that consumes the service
                    String requestUri = routingContext.request().uri().split("/" + GATEWAY_NAME)[1];
                    System.out.println("CREATING REQUEST " + routingContext.request().uri().split("/" + GATEWAY_NAME)[1]);

                    //instantiate client and handler for response
                    HttpClientRequest request = client.request(routingContext.request().method(), requestUri, response -> response.bodyHandler(body -> {
                        //this response is the HttpEndPoint service consumer response, not the original client
                        System.out.println("RESPONSE GATEWAY: " + response.statusCode() + " " + body.toString() + " " + routingContext.request().uri());
                        HttpServerResponse rsp = routingContext.response(); //response for original client
                        rsp.setStatusCode(response.statusCode());
                        response.headers().forEach(header -> rsp.putHeader(header.getKey(), header.getValue()));
                        rsp.end(body.toString("UTF-8"), "UTF-8"); //write a message for the HttpResponse on the original client side
                    }));
                    //send request data if needed
                    if (routingContext.getBodyAsJson() == null) {
                        request.end();
                        ServiceDiscovery.releaseServiceObject(discovery, client);
                    } else {
                        request.end(routingContext.getBody());
                        ServiceDiscovery.releaseServiceObject(discovery, client);

                    }
                }


            } else {
                System.out.println("error cant find services " + routingContext.request().uri());
                routingContext.response().end();

            }


        });
    }

    private void functioningapachejena() throws FileNotFoundException {
        String personURI = "http://somewhere/JohnSmith";
        String givenName = "John";
        String familyName = "Smith";
        String fullName = givenName + " " + familyName;

// create an empty Model
        Model model = ModelFactory.createDefaultModel();

// create the resource
//   and add the properties cascading style
        Resource johnSmith
                = model.createResource(personURI)
                .addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N,
                        model.createResource()
                                .addProperty(VCARD.Given, givenName)
                                .addProperty(VCARD.Family, familyName));
        OutputStream outputStream = new FileOutputStream("D:\\Repos\\RecommenderFood\\src\\main\\resources\\test.rdf");

        model.write(outputStream);
        model.write(System.out, "RDF/XML-ABBREV");
    }
}
