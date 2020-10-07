package zxcv.jvmmx.api.email;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EmailVerticle extends AbstractVerticle {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Random random = new Random();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.eventBus().<String>consumer("email -> notifyPayment", message -> {
      Single.just(message)
        .delay(random.nextInt(5000), TimeUnit.MILLISECONDS)
        .doOnSuccess(n -> log.info("Se mando un correo -> txId {}", n.body()))
        .subscribe();
    });

  }
}
