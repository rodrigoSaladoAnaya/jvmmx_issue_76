package zxcv.jvmmx.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiVerticle extends AbstractVerticle {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    vertx.deployVerticle("maven:zxcv.jvmmx:webservice:1.0::service");
    vertx.deployVerticle("maven:zxcv.jvmmx:database:1.0::service");
    vertx.deployVerticle("maven:zxcv.jvmmx:email:1.0::service");

    log.info("+++ ApiVerticle inicido");
    startPromise.complete();
  }
}
