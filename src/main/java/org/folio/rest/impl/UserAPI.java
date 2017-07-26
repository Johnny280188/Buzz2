package org.folio.rest.impl;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.rest.jaxrs.model.User;
import org.folio.rest.jaxrs.model.Users;
import org.folio.rest.jaxrs.resource.UsersResource;
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
public class UserAPI implements UsersResource {

  public static final String Q_TABLE = "users";
  private static final Logger       log                   = LoggerFactory.getLogger(UserAPI.class);
  private static final String tenantId = Consts.TENANT_ID;

  public UserAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, Consts.TENANT_ID).setIdField("id");
  }

  @Override
  public void getUsers(String query, int offset, int limit, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    try {
      CQLWrapper cql = Consts.getCQL(query,limit,offset);
      String[] fieldList = {"*"};
      PostgresClient.getInstance(vertxContext.owner(), tenantId).get(Q_TABLE,
        User.class, fieldList, cql, true, true, reply -> {
        try {
          if(reply.succeeded()) {
            Users users = new Users();
            List<User> user = (List<User>)reply.result()[0];
            users.setUser(user);
            users.setTotalRecords((Integer)reply.result()[1]);
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
              GetUsersResponse.withJsonOK(users)));
          } else {
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
              GetUsersResponse.withPlainInternalServerError(reply.cause().getMessage())));
            log.error(reply.cause().getMessage(), reply.cause());
          }
        } catch(Exception e){
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            GetUsersResponse.withPlainInternalServerError(e.getMessage())));
          log.error(e.getMessage(), e);
        }
      });
    }
    catch(Exception e1){
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        GetUsersResponse.withPlainInternalServerError(e1.getMessage())));
      log.error(e1.getMessage(), e1);
    }
  }

  @Override
  public void postUsers(User entity, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    try {
      PostgresClient.getInstance(vertxContext.owner(), tenantId).save(Q_TABLE, entity, rep -> {
        if(rep.succeeded()){
          OutStream os = new OutStream();
          os.setData(entity);
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            PostUsersResponse.withJsonCreated("/users/"+rep.result(), os)));
        }else{
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            PostUsersResponse.withPlainInternalServerError(rep.cause().getMessage())));
          log.error(rep.cause().getMessage(), rep.cause());
        }

      });
    } catch (Exception e) {
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        PostUsersResponse.withPlainInternalServerError(e.getMessage())));
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public void getUsersByUserId(String userId, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    vertxContext.runOnContext(v -> {
      try {
        Criterion c = new Criterion(
          new Criteria().addField("id").setJSONB(false).setOperation("=").setValue("'"+userId+"'"));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(Q_TABLE, User.class, c, true,
            reply -> {
              try {
                if(reply.succeeded()){
                  @SuppressWarnings("unchecked")
                  List<User> users = (List<User>) reply.result()[0];
                  if(users.isEmpty()){
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetUsersByUserIdResponse
                      .withPlainNotFound(userId)));
                  }
                  else{
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetUsersByUserIdResponse
                      .withJsonOK(users.get(0))));
                  }
                }
                else{
                  log.error(reply.cause().getMessage(), reply.cause());
                  if(Consts.isInvalidUUID(reply.cause().getMessage())){
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetUsersByUserIdResponse
                      .withPlainNotFound(userId)));
                  }
                  else{
                    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetUsersByUserIdResponse
                      .withPlainInternalServerError(reply.cause().getMessage())));
                  }
                }
              } catch (Exception e) {
                log.error(e.getMessage(), e);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetUsersByUserIdResponse
                  .withPlainInternalServerError(e.getMessage())));
              }
        });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetUsersByUserIdResponse
          .withPlainInternalServerError(e.getMessage())));
      }
    });
  }

  @Override
  public void deleteUsersByUserId(String userId, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    vertxContext.runOnContext(v -> {
      try {
        PostgresClient.getInstance(vertxContext.owner(), tenantId).delete(Q_TABLE, userId,
          reply -> {
            try {
              if(reply.succeeded()){
                if(reply.result().getUpdated() == 1){
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteUsersByUserIdResponse
                    .withNoContent()));
                }
                else{
                  log.error("records updated = " + reply.result().getUpdated());
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteUsersByUserIdResponse
                    .withPlainNotFound("Records updated: "+reply.result().getUpdated())));
                }
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteUsersByUserIdResponse
                  .withPlainInternalServerError(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteUsersByUserIdResponse
                .withPlainInternalServerError(e.getMessage())));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(DeleteUsersByUserIdResponse
          .withPlainInternalServerError(e.getMessage())));
      }
    });
  }

  @Override
  public void putUsersByUserId(String userId, User entity, Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    vertxContext.runOnContext(v -> {
      try {
        PostgresClient.getInstance(vertxContext.owner(), tenantId).update(
          Q_TABLE, entity, userId,
          reply -> {
            try {
              if(reply.succeeded()){
                if(reply.result().getUpdated() == 0){
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutUsersByUserIdResponse
                    .withPlainNotFound("No records updated")));
                }
                else{
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutUsersByUserIdResponse
                    .withNoContent()));
                }
              }
              else{
                log.error(reply.cause().getMessage());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutUsersByUserIdResponse
                  .withPlainInternalServerError(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutUsersByUserIdResponse
                .withPlainInternalServerError(e.getMessage())));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(PutUsersByUserIdResponse
          .withPlainInternalServerError(e.getMessage())));
      }
    });

  }

}
