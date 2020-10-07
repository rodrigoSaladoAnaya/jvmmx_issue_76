package org.jvmmx.manager;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import jdk.jshell.SourceCodeAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerMainVerticle extends AbstractVerticle {

  Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var service = Service.create(vertx);
    Single.just(service)
      .flatMap(Service::repo)
      .flatMap(Service::watchService)
      .flatMap(Service::registerRepo)
      .flatMap(Service::touchFiles)
      .flatMap(Service::worker)
      .doOnError(e -> log.error("Manager", e))
      .subscribe(
        n -> startPromise.complete(),
        e -> startPromise.fail(e)
      );
  }
}
