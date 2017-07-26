package org.folio.rest.utils;

import java.text.SimpleDateFormat;

import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.cql.CQLWrapper;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;
import org.z3950.zing.cql.cql2pgjson.FieldException;

/**
 * @author shale
 *
 */
public class Consts {

  public static final String TENANT_ID = "buzz99";
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");



  public static CQLWrapper getCQL(String query, int limit, int offset) throws FieldException {
    CQL2PgJSON cql2pgJson = new CQL2PgJSON("questions.jsonb");
    return new CQLWrapper(cql2pgJson, query).setLimit(new Limit(limit)).setOffset(new Offset(offset));
  }

  public static boolean isInvalidUUID(String errorMessage){
    if(errorMessage != null && errorMessage.contains("invalid input syntax for uuid")){
      return true;
    }
    else{
      return false;
    }
  }

}
