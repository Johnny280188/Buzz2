package org.folio.rest.impl;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Channel;
import org.folio.rest.jaxrs.model.Channels;
import org.folio.rest.jaxrs.resource.ChannelResource;
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
public class ChannelAPI implements ChannelResource {

  public static final String Q_TABLE = "channels";
  private static final Logger log     = LoggerFactory.getLogger(ChannelAPI.class);
  private static final String tenantId = Consts.TENANT_ID;

  public ChannelAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, Consts.TENANT_ID).setIdField("id");
  }

  @Validate
  @Override
  public void getChannel(String query, int offset, int limit, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    try {
      CQLWrapper cql = Consts.getCQL(query,limit,offset);
      String[] fieldList = {"*"};
      PostgresClient.getInstance(vertxContext.owner(), tenantId).get(Q_TABLE,
        Channel.class, fieldList, cql, true, true, reply -> {
        try {
          if(reply.succeeded()) {
            Channels channels = new Channels();
            List<Channel> channel = (List<Channel>)reply.result()[0];
            channels.setChannel(channel);
            channels.setTotalRecords((Integer)reply.result()[1]);
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
              GetChannelResponse.withJsonOK(channels)));
          } else {
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
              GetChannelResponse.withPlainInternalServerError(reply.cause().getMessage())));
            log.error(reply.cause().getMessage(), reply.cause());
          }
        } catch(Exception e){
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            GetChannelResponse.withPlainInternalServerError(e.getMessage())));
          log.error(e.getMessage(), e);
        }
      });
    }
    catch(Exception e1){
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        GetChannelResponse.withPlainInternalServerError(e1.getMessage())));
      log.error(e1.getMessage(), e1);
    }

  }

  @Validate
  @Override
  public void postChannel(Channel entity, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    try {
      PostgresClient.getInstance(vertxContext.owner(), tenantId).save(Q_TABLE, entity, rep -> {
        if(rep.succeeded()){
          OutStream os = new OutStream();
          os.setData(entity);
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            PostChannelResponse.withJsonCreated("/channel/"+rep.result(), os)));
        }else{
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            PostChannelResponse.withPlainInternalServerError(rep.cause().getMessage())));
          log.error(rep.cause().getMessage(), rep.cause());
        }

      });
    } catch (Exception e) {
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        PostChannelResponse.withPlainInternalServerError(e.getMessage())));
      log.error(e.getMessage(), e);
    }
  }

  @Validate
  @Override
  public void getChannelByChannelId(String channelId, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    vertxContext.runOnContext(v -> {
      try {
        Criterion c = new Criterion(
          new Criteria().addField("id").setJSONB(false).setOperation("=").setValue("'"+channelId+"'"));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(Q_TABLE, Channel.class, c, true,
            reply -> {
              try {
                if(reply.succeeded()){
                  @SuppressWarnings("unchecked")
                  List<Channel> channels = (List<Channel>) reply.result()[0];
                  if(channels.isEmpty()){
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetChannelByChannelIdResponse
                      .withPlainNotFound(channelId)));
                  }
                  else{
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetChannelByChannelIdResponse
                      .withJsonOK(channels.get(0))));
                  }
                }
                else{
                  log.error(reply.cause().getMessage(), reply.cause());
                  if(Consts.isInvalidUUID(reply.cause().getMessage())){
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetChannelByChannelIdResponse
                      .withPlainNotFound(channelId)));
                  }
                  else{
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetChannelByChannelIdResponse
                      .withPlainInternalServerError(reply.cause().getMessage())));
                  }
                }
              } catch (Exception e) {
                log.error(e.getMessage(), e);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetChannelByChannelIdResponse
                  .withPlainInternalServerError(e.getMessage())));
              }
        });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetChannelByChannelIdResponse
          .withPlainInternalServerError(e.getMessage())));
      }
    });

  }

  @Validate
  @Override
  public void deleteChannelByChannelId(String channelId, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    vertxContext.runOnContext(v -> {
      try {
        PostgresClient.getInstance(vertxContext.owner(), tenantId).delete(Q_TABLE, channelId,
          reply -> {
            try {
              if(reply.succeeded()){
                if(reply.result().getUpdated() == 1){
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteChannelByChannelIdResponse
                    .withNoContent()));
                }
                else{
                  log.error("records updated = " + reply.result().getUpdated());
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteChannelByChannelIdResponse
                    .withPlainNotFound("Records updated: "+reply.result().getUpdated())));
                }
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteChannelByChannelIdResponse
                  .withPlainInternalServerError(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteChannelByChannelIdResponse
                .withPlainInternalServerError(e.getMessage())));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteChannelByChannelIdResponse
          .withPlainInternalServerError(e.getMessage())));
      }
    });
  }

  @Validate
  @Override
  public void putChannelByChannelId(String channelId, Channel entity,
      Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      try {
        PostgresClient.getInstance(vertxContext.owner(), tenantId).update(
          Q_TABLE, entity, channelId,
          reply -> {
            try {
              if(reply.succeeded()){
                if(reply.result().getUpdated() == 0){
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutChannelByChannelIdResponse
                    .withPlainNotFound("No records updated")));
                }
                else{
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutChannelByChannelIdResponse
                    .withNoContent()));
                }
              }
              else{
                log.error(reply.cause().getMessage());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutChannelByChannelIdResponse
                  .withPlainInternalServerError(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutChannelByChannelIdResponse
                .withPlainInternalServerError(e.getMessage())));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutChannelByChannelIdResponse
          .withPlainInternalServerError(e.getMessage())));
      }
    });
  }

}
