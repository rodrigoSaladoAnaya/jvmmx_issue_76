package zxcv.jvmmx.api;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiVerticle extends AbstractVerticle {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Single.just(true)
      .flatMap(n -> deploy("maven:zxcv.jvmmx:webservice:1.0::service"))
      .flatMap(n -> deploy("maven:zxcv.jvmmx:database:1.0::service"))
      .flatMap(n -> deploy("maven:zxcv.jvmmx:email:1.0::service"))
      .doOnSuccess(n -> log.info("+++ ApiVerticle inicido"))
      .subscribe(
        n -> startPromise.complete(),
        e -> startPromise.fail(e)
      );
  }

  private Single<String> deploy(String verticle) {
    return Single.create(emitter -> {
      vertx.deployVerticle(verticle, ar -> {
        if(ar.succeeded()) {
          emitter.onSuccess(ar.result());
        } else {
          emitter.onError(ar.cause());
        }
      });
    });
  }
}
