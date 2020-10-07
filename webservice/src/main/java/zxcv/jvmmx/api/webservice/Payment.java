package zxcv.jvmmx.api.webservice;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Payment {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final RoutingContext context;
  private final Vertx vertx;
  private final JsonObject jsonMessage;
  private String txId;

  private Payment(RoutingContext context) {
    this.context = context;
    this.vertx = context.vertx();
    this.jsonMessage = new JsonObject();
  }

  private Single<Payment> config() {
    context.queryParams().forEach(p -> jsonMessage.put(p.getKey(), p.getValue()));
    context.response().putHeader("content-type", "text/plain");
    return Single.just(this);
  }

  private Single<Payment> callDatabase() {
    return Single.create(emitter -> {
      vertx.eventBus().<String>request("database -> notifyPayment", jsonMessage, ar -> {
        if(ar.succeeded()) {
          txId = ar.result().body();
          log.info("Nos llego un TX_ID: {}", txId);
          emitter.onSuccess(this);
        } else {
          emitter.onError(ar.cause());
        }
      });
    });
  }

  private Single<Payment> sendEmail() {
    vertx.eventBus().send("email -> notifyPayment", txId);
    return Single.just(this);
  }

  private Single<Payment> responseToClient() {
    context.response().end(txId);
    return Single.just(this);
  }

  private Payment responseErrorToClient(Throwable error) {
    context.response().end("Internal Error");
    log.error("Internal Error", error);
    return this;
  }

  public static Disposable create(RoutingContext context) {
    var payment = new Payment(context);
    return Single.just(payment)
      .flatMap(Payment::config)
      .flatMap(Payment::callDatabase)
      .flatMap(Payment::sendEmail)
      .flatMap(Payment::responseToClient)
      .onErrorReturn(payment::responseErrorToClient)
      .subscribe();
  }
}
