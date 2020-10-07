package org.jvmmx.manager;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class Worker {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  private static Vertx vertx;
  private final WatchKey watchKey;
  private List<WatchEvent<?>> watchEvents;

  private Worker(WatchKey watchKey) {
    this.watchKey = watchKey;
  }

  public Worker pollEvents() {
    watchEvents = watchKey.pollEvents();
    return this;
  }

  public Worker processEvents() {
    watchEvents.forEach(we -> {
      var event = Event.create(vertx, watchKey, we);
      Single.just(event)
        .map(Event::path)
        .map(Event::fileName)
        .flatMapMaybe(Event::validateExtension)
        .flatMap(n -> Maybe.merge(
          n.filterDeploy(),
          n.filterUndeploy()
        ).firstElement())
        .onErrorReturn(event::onError)
        .subscribe();
    });
    return this;
  }

  public Worker resetWatcher() {
    watchKey.reset();
    return this;
  }

  public static Worker create(WatchKey watchKey) {
    return new Worker(watchKey);
  }

  private static void setVertx(Vertx vertx) {
    Worker.vertx =  vertx;
  }

  public static Observable<WatchKey> eventsEmitter(Vertx vertx, WatchService watch) {
    setVertx(vertx);
    return Observable.create(emitter -> {
      while (!emitter.isDisposed()) {
        emitter.onNext(watch.take());
      }
    });
  }
}
