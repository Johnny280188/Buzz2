package org.folio.rest.impl;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Buzz;
import org.folio.rest.jaxrs.model.Buzzes;
import org.folio.rest.jaxrs.resource.BuzzResource;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.utils.Consts;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author shale
 *
 */
public class BuzzAPI implements BuzzResource {

  public static final String Q_TABLE = "questions";
  private static final SimpleDateFormat DATE_FORMAT = Consts.DATE_FORMAT;
  private static final Logger       log                   = LoggerFactory.getLogger(BuzzAPI.class);
  private static final String tenantId = Consts.TENANT_ID;

  public BuzzAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, Consts.TENANT_ID).setIdField("id");
  }

  @Validate
  @Override
  public void getBuzz(String query, int offset, int limit, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    try {
      CQLWrapper cql = Consts.getCQL(query,limit,offset);
      String[] fieldList = {"*"};
      PostgresClient.getInstance(vertxContext.owner(), tenantId).get(Q_TABLE,
        Buzz.class, fieldList, cql, true, true, reply -> {
        try {
          if(reply.succeeded()) {
            Buzzes buzzes = new Buzzes();
            List<Buzz> buzz = (List<Buzz>)reply.result()[0];
            buzzes.setBuzz(buzz);
            buzzes.setTotalRecords((Integer)reply.result()[1]);
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
              GetBuzzResponse.withJsonOK(buzzes)));
          } else {
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
              GetBuzzResponse.withPlainInternalServerError(reply.cause().getMessage())));
          }
        } catch(Exception e){
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            GetBuzzResponse.withPlainInternalServerError(e.getMessage())));
        }
      });
    }
    catch(Exception e1){
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        GetBuzzResponse.withPlainInternalServerError(e1.getMessage())));
    }
  }

  @Validate
  @Override
  public void postBuzz(Buzz entity, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    try {
      PostgresClient.getInstance(vertxContext.owner(), tenantId).save(Q_TABLE, entity, rep -> {
        if(rep.succeeded()){
          OutStream os = new OutStream();
          os.setData(entity);
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            PostBuzzResponse.withJsonCreated("/buzz/"+rep.result(), os)));
        }else{
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            PostBuzzResponse.withPlainInternalServerError(rep.cause().getMessage())));
        }
      });
    } catch (Exception e) {
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        PostBuzzResponse.withPlainInternalServerError(e.getMessage())));
      log.error(e.getMessage(), e);
    }
  }

  @Validate
  @Override
  public void getBuzzByBuzzId(String buzzId, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      try {
        Criterion c = new Criterion(
          new Criteria().addField("id").setJSONB(false).setOperation("=").setValue("'"+buzzId+"'"));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(Q_TABLE, Buzz.class, c, true,
            reply -> {
              try {
                if(reply.succeeded()){
                  @SuppressWarnings("unchecked")
                  List<Buzz> userGroup = (List<Buzz>) reply.result()[0];
                  if(userGroup.isEmpty()){
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetBuzzByBuzzIdResponse
                      .withPlainNotFound(buzzId)));
                  }
                  else{
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetBuzzByBuzzIdResponse
                      .withJsonOK(userGroup.get(0))));
                  }
                }
                else{
                  log.error(reply.cause().getMessage(), reply.cause());
                  if(Consts.isInvalidUUID(reply.cause().getMessage())){
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetBuzzByBuzzIdResponse
                      .withPlainNotFound(buzzId)));
                  }
                  else{
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetBuzzByBuzzIdResponse
                      .withPlainInternalServerError(reply.cause().getMessage())));
                  }
                }
              } catch (Exception e) {
                log.error(e.getMessage(), e);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetBuzzByBuzzIdResponse
                  .withPlainInternalServerError(e.getMessage())));
              }
        });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetBuzzByBuzzIdResponse
          .withPlainInternalServerError(e.getMessage())));
      }
    });
  }

  @Validate
  @Override
  public void deleteBuzzByBuzzId(String buzzId, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      try {
        PostgresClient.getInstance(vertxContext.owner(), tenantId).delete(Q_TABLE, buzzId,
          reply -> {
            try {
              if(reply.succeeded()){
                if(reply.result().getUpdated() == 1){
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteBuzzByBuzzIdResponse
                    .withNoContent()));
                }
                else{
                  log.error("records updated = " + reply.result().getUpdated());
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteBuzzByBuzzIdResponse
                    .withPlainNotFound("Records updated: "+reply.result().getUpdated())));
                }
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteBuzzByBuzzIdResponse
                  .withPlainInternalServerError(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteBuzzByBuzzIdResponse
                .withPlainInternalServerError(e.getMessage())));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteBuzzByBuzzIdResponse
          .withPlainInternalServerError(e.getMessage())));
      }
    });
  }

  @Validate
  @Override
  public void putBuzzByBuzzId(String buzzId, Buzz entity, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      try {
        PostgresClient.getInstance(vertxContext.owner(), tenantId).update(
          Q_TABLE, entity, buzzId,
          reply -> {
            try {
              if(reply.succeeded()){
                if(reply.result().getUpdated() == 0){
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutBuzzByBuzzIdResponse
                    .withPlainNotFound("No records updated")));
                }
                else{
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutBuzzByBuzzIdResponse
                    .withNoContent()));
                }
              }
              else{
                log.error(reply.cause().getMessage());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutBuzzByBuzzIdResponse
                  .withPlainInternalServerError(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutBuzzByBuzzIdResponse
                .withPlainInternalServerError(e.getMessage())));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutBuzzByBuzzIdResponse
          .withPlainInternalServerError(e.getMessage())));
      }
    });
  }

}
