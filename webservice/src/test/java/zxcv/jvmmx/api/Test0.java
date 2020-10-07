package zxcv.jvmmx.api;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import rx.Single;
import zxcv.jvmmx.api.database.DatabaseVerticle;
import zxcv.jvmmx.api.webservice.WebserviceVerticle;

@ExtendWith(VertxExtension.class)
public class Test0 {

  @Test
  void test0(Vertx vert, VertxTestContext context) {
    var ws =Single.create(emitter -> {
      vert.deployVerticle(new WebserviceVerticle())
    });

    var bd = Single.create(emitter -> {
      vert.deployVerticle(new DatabaseVerticle());
    });
  }

}
