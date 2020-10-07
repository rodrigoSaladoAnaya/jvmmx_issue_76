package org.jvmmx.manager;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

import static java.nio.file.StandardWatchEventKinds.*;

public class Event {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Vertx vertx;
  private final WatchKey key;
  private final WatchEvent<?> event;
  private Path path;
  private String fileName;
  private JsonObject json;
  private String module;
  private DeploymentOptions options;

  private Event(Vertx vertx, WatchKey key, WatchEvent<?> event) {
    this.vertx = vertx;
    this.key = key;
    this.event = event;
  }

  public Event path() {
    path = (Path) event.context();
    return this;
  }

  public Event fileName() {
    fileName = path.toFile().getName();
    return this;
  }

  public Maybe<Event> validateExtension() {
    return Maybe.just(this)
      .filter(n -> fileName.toLowerCase().endsWith(".json"));
  }

  public Maybe<Event> filterDeploy() {
    return Maybe.just(this)
      .filter(n -> event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY && !Service.installed.containsKey(fileName))
      .doOnSuccess(n -> log.info("deploy -> event :: {} {}", n.event.kind(), fileName))
      .map(Event::config)
      .map(Event::module)
      .flatMap(Event::deploy);
  }

  public Maybe<Event> filterUndeploy() {
    return Maybe.just(this)
      .filter(n -> event.kind() == ENTRY_DELETE && Service.installed.containsKey(fileName))
      .doOnSuccess(n -> log.info("undeploy -> event :: {} {}", n.event.kind(), fileName))
      .flatMap(Event::undeploy);
  }

  public Event config() throws IOException {
    var dir = (Path) key.watchable();
    Path fullPath = dir.resolve((Path) event.context());
    var allLines = Files.readAllLines(fullPath);
    var joinLines = String.join("\n", allLines);
    json = new JsonObject(joinLines);
    return this;
  }

  public Event module() {
    module = json.getString("module");
    var config = json.getJsonObject("config");
    options = new DeploymentOptions().setConfig(config);
    return this;
  }

  public Maybe<Event> deploy() {
    return Maybe.create(emitter -> {
      vertx.deployVerticle(module, options, ar -> {
        if(ar.succeeded()) {
          var deploymentId = ar.result();
          log.info("Se instalo el modulo [{}] -> {}", fileName, deploymentId);
          Service.installed.put(fileName, deploymentId);
          emitter.onSuccess(this);
        } else {
          emitter.onError(ar.cause());
        }
      });
    });
  }

  public Maybe<Event> undeploy() {
    return Maybe.create(emitter -> {
      vertx.undeploy(Service.installed.get(fileName), ar -> {
        if(ar.succeeded()) {
          var deploymentId = Service.installed.remove(fileName);
          log.info("Se desinstala el modulo [{}] -> {}", fileName, deploymentId);
          emitter.onSuccess(this);
        } else {
          emitter.onError(ar.cause());
        }
      });
    });
  }

  public Event onError(Throwable error) {
    log.error("Error al leer archivo [{}] {}", fileName , error);
    return this;
  }

  public static Event create(Vertx vertx, WatchKey key, WatchEvent<?> event) {
    return new Event(vertx, key, event);
  }
}
