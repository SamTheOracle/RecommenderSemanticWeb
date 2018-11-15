package food.recommender.user.accountmicroservice.EventBusProxy;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface UserAccountService {

    static UserAccountService create(Vertx vertx) {
        return new UserAccountServiceImpl(vertx);
    }

    static UserAccountService createProxy(Vertx vertx,
                                           String address) {
        return new UserAccountServiceVertxEBProxy(vertx,address);
    }

    void save(String collection, JsonObject document, Handler<AsyncResult<String>> resultHandler);

}
