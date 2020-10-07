package zxcv.jvmmx.api.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DatabaseVerticle extends AbstractVerticle {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.eventBus().<JsonObject>consumer("database -> notifyPayment", message -> {
      var txId = UUID.randomUUID().toString();
      log.info("Se solicito un TXID  -> {} {}", txId, message.body());
      message.reply(String.format("(V1.1) %s", txId));
    }).completionHandler(ar -> startPromise.complete());
  }

}
