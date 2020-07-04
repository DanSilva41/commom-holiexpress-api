package com.holiexpress.ecommerce.common;

import com.holiexpress.ecommerce.common.exceptions.HoliExpressApiException;
import io.vertx.core.*;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.docker.DockerLinksServiceImporter;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link Verticle} taking care of the discovery and publication of services.
 *
 * @author <a href="https://dansilva41.github.io/" target="_blank">Danilo Silva</a>
 */
public class CustomAbstractVerticle extends AbstractVerticle {

  protected ServiceDiscovery discovery;
  protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

  @Override
  public void start(Promise<Void> start) {
    discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));
    discovery.registerServiceImporter(new DockerLinksServiceImporter(), new JsonObject());
  }

  protected void publishMessageSource(String name, String address, Class contentClass, Handler<AsyncResult<Void>>
    completionHandler) {
    Record record = MessageSource.createRecord(name, address, contentClass);
    publish(completionHandler, record);
  }

  protected void publishMessageSource(String name, String address, Handler<AsyncResult<Void>>
    completionHandler) {
    Record record = MessageSource.createRecord(name, address);
    publish(completionHandler, record);
  }

  protected void publishEventBusService(String name, String address, Class serviceClass, Handler<AsyncResult<Void>>
    completionHandler) {
    Record record = EventBusService.createRecord(name, address, serviceClass);
    publish(completionHandler, record);
  }

  private void publish(Handler<AsyncResult<Void>> completionHandler, Record record) {
    if (discovery == null)
      try {
        start();
      } catch (Exception e) {
        throw new HoliExpressApiException("Cannot create discovery service!");
      }

    discovery.publish(record, ar -> {
      if (ar.succeeded()) {
        registeredRecords.add(record);
        completionHandler.handle(Future.succeededFuture());
      } else {
        completionHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void stop(Promise<Void> done) {
    List<Future> futures = new ArrayList<>();
    for (Record record : registeredRecords) {
      Promise<Void> unregistrationFuture = Promise.promise();
      futures.add(unregistrationFuture.future());
      discovery.unpublish(record.getRegistration(), unregistrationFuture);
    }
    registeredRecords.clear();

    CompositeFuture.all(futures).onComplete(x -> {
      discovery.close();
      if (x.failed()) {
        done.fail(x.cause());
      } else {
        done.complete();
      }
    });
  }
}
