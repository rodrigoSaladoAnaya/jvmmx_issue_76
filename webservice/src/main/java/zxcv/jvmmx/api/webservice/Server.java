package zxcv.jvmmx.api.webservice;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Vertx vertx;
  private HttpServer server;
  private Router router;

  private Server(Vertx vertx) {
    this.vertx = vertx;
  }

  private Single<Server> createServer() {
    server = vertx.createHttpServer();
    return Single.just(this);
  }

  private Single<Server> router() {
    router = Router.router(vertx);
    return Single.just(this);
  }

  private Single<Server> payment() {
    router.route("/payment").handler(Payment::create);
    return Single.just(this);
  }

  private Single<Server> listen() {
    return Single.create(emitter -> {
      server.requestHandler(router).listen(8080, ar -> {
        if(ar.succeeded()) {
          log.info("Webservice iniciado en el puerto {}", ar.result().actualPort());
          emitter.onSuccess(this);
        } else {
          emitter.onError(ar.cause());
        }

      });
    });
  }

  public static Single<Server> create(Vertx vertx) {
    var server = new Server(vertx);
    return Single.just(server)
      .flatMap(Server::createServer)
      .flatMap(Server::router)
      .flatMap(Server::payment)
      .flatMap(Server::listen);
  }
}
