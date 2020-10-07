package zxcv.jvmmx.api.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebserviceVerticle extends AbstractVerticle {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var config = context.config();
    Server.create(vertx, config)
      .doOnSuccess(n -> log.info("+++ WebserviceVerticle inicido"))
      .subscribe(
        n -> startPromise.complete(),
        e -> startPromise.fail(e)
      );
  }
}
