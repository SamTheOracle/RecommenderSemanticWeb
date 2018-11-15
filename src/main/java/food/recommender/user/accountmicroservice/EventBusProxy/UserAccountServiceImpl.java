package food.recommender.user.accountmicroservice.EventBusProxy;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class UserAccountServiceImpl implements UserAccountService {
    private Vertx vertx;
    private MongoClient mongoClient;

    public UserAccountServiceImpl(Vertx vertx) {
        this.vertx = vertx;

    }

    @Override
    public void save(String collection, JsonObject document, Handler<AsyncResult<String>> resultHandler) {
        System.out.println("Inserted id: " + document);
        mongoClient = MongoClient.createShared(vertx, new JsonObject()

                .put("connection_string", "mongodb://giacomo:metallaro93@ds043158.mlab.com:43158/healthcare_and_food")
                .put("db_name", "healthcare_and_food"));
        mongoClient.save(collection, document, event2 -> {
            if (event2.succeeded()) {
                resultHandler.handle(Future.succeededFuture("dsfgr"));



            } else {
                System.out.println(event2.cause().getMessage());
            }
        });
    }
}
