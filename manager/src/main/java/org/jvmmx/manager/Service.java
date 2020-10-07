package org.jvmmx.manager;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.WatchService;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardWatchEventKinds.*;

public class Service {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  private final Vertx vertx;
  private File repoFile;
  private WatchService watch;
  public static final ConcurrentHashMap<String, String> installed = new ConcurrentHashMap<>();

  private Service(Vertx vertx) {
    this.vertx = vertx;
  }

  public Single<Service> repo() {
    repoFile = new File("./repo");
    return Single.just(this);
  }

  public Single<Service> watchService() throws IOException {
    watch = FileSystems.getDefault().newWatchService();
    return Single.just(this);
  }

  public Single<Service> registerRepo() throws IOException {
    repoFile.toPath().register(watch, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    return Single.just(this);
  }

  public Single<Service> touchFiles() throws IOException {
    Files.list(repoFile.toPath()).forEach(p -> p.toFile().setLastModified(System.currentTimeMillis()));
    return Single.just(this);
  }

  public Single<Service> worker() {
    Worker.eventsEmitter(vertx, watch)
      .map(Worker::create)
      .map(Worker::pollEvents)
      .map(Worker::processEvents)
      .map(Worker::resetWatcher)
      .subscribeOn(Schedulers.computation())
      .subscribe();
    return Single.just(this);
  }

  public static Service create(Vertx vertx) {
    return new Service(vertx);
  }
}
