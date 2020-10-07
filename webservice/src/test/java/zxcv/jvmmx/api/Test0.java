package zxcv.jvmmx.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;
import zxcv.jvmmx.api.database.DatabaseVerticle;
import zxcv.jvmmx.api.webservice.WebserviceException;
import zxcv.jvmmx.api.webservice.WebserviceVerticle;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class Test0 {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Test
  @DisplayName("Se valida que se mande un puerto en al configuracion")
  void test0(Vertx vertx, VertxTestContext testContext) {
    var config = new JsonObject();
    deployVerticle(vertx, config, new WebserviceVerticle())
      .doOnSuccess(n -> {
        assertTrue(n.failed());
        assertNotNull(n.cause());
        assertTrue(n.cause() instanceof WebserviceException);
      })
      .subscribe(
        n -> testContext.completeNow(),
        e -> testContext.failNow(e)
      );
  }

  @Test
  @DisplayName("Se valida que se el puerto en configuracion sea un Integer")
  void test1(Vertx vertx, VertxTestContext testContext) {
    var config = new JsonObject()
      .put("port", "SSSS");
    deployVerticle(vertx, config, new WebserviceVerticle())
      .doOnSuccess(n -> {
        assertTrue(n.failed());
        assertNotNull(n.cause());
        assertTrue(n.cause() instanceof WebserviceException);
      })
      .subscribe(
        n -> testContext.completeNow(),
        e -> testContext.failNow(e)
      );
  }

  @Test
  @DisplayName("Se valida que arroje un error cuando no se tiene instalado el verticle Database")
  void test2(Vertx vertx, VertxTestContext testContext) {
    var config = new JsonObject()
      .put("port", 8088);
    deployVerticle(vertx, config, new WebserviceVerticle())
      .flatMap(n -> httpRequest(vertx, config))
      .doOnSuccess(n -> assertEquals(n.result().body().toString(), "Internal Error"))
      .subscribe(
        n -> testContext.completeNow(),
        e -> testContext.failNow(e)
      );
  }


  @Test
  @DisplayName("Se valida que una respuesta OK")
  void test3(Vertx vertx, VertxTestContext testContext) {
    var config = new JsonObject()
      .put("port", 8088);
    Single.just(true)
      .flatMap(n -> deployVerticle(vertx, config, new WebserviceVerticle()))
      .flatMap(n -> deployVerticle(vertx, config, new DatabaseVerticle()))
      .flatMap(n -> httpRequest(vertx, config))
      .doOnSuccess(n -> assertNotEquals(n.result().body().toString(), "Internal Error"))
      .subscribe(
        n -> testContext.completeNow(),
        e -> testContext.failNow(e)
      );
  }


  //=========================================================================

  private static <T extends AbstractVerticle> Single<AsyncResult<String>> deployVerticle(Vertx vertx, JsonObject config, T verticle) {
    var options = new DeploymentOptions()
      .setConfig(config);
    return Single.create(emitter -> vertx.deployVerticle(verticle, options, emitter::onSuccess));
  }

  private static Single<AsyncResult<HttpResponse<Buffer>>> httpRequest(Vertx vertx, JsonObject config) {
    var web = WebClient.create(vertx);
    return Single.create(emitter -> {
      web.get(config.getInteger("port"), "localhost",  "/payment")
      .send(emitter::onSuccess);
    });
  }
  
  private static <T, V> Single<AsyncResult<Message<V>>> request(Vertx vertx, String address, T message) {
    return Single.create(emitter -> vertx.eventBus().request(address, message, emitter::onSuccess));
  }

}
