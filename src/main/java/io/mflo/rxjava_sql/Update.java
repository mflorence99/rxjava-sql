package io.mflo.rxjava_sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model SQL DELETE/INSERT/UPDATE
 *
 * <p>An Uopdate is created by the <code>SQL.update</code> method.
 * A fluent API configures the Update until it is ready to {@link #execute}.</p>
 *
 * @author      http://mflo.io
 * @version     0.0.1
 */
public final class Update implements Parameters {

  // logger for this class
  private static final Logger log = LoggerFactory.getLogger(Update.class);

  // private state
  private final String sql;
  private final DataSource ds;

  // state accumulated by fluent API
  // NOTE: execute should never modify these fields, so a Update can be reused
  private Map<String,Object> parameters = new HashMap();

  /**
   * Private ctor: use <code>SQL.update</code>
   *
   * @param   sql SQL DELETE/INSERT/UPDATE statement
   * @param   ds data source
   *
   * @see     SQL#update to construct from a string representation
   */
  protected Update(String sql,
                   DataSource ds) {
    this.sql = sql;
    this.ds = ds;
  }

  /**
   * Execute this <code>Update</code>
   *
   * @return  count number of rows affected
   *
   * @throws  SQLException when any SQL statement fails
   */
  public int execute() throws SQLException {
    int count = 0;
    try (Connection connection = ds.getConnection()) {
      Map<String,Integer> ordinalByName = new HashMap<>();
      String prepared = orderParameterNames(sql, ordinalByName);
      try (PreparedStatement stmt = connection.prepareStatement(prepared)) {
        injectParameters(stmt, parameters, ordinalByName);
        log.debug(stmt.toString());
        count = stmt.executeUpdate();
      }
    }
    return count;
  }

  /**
   * Fluent API to configure Query with positional parameter values. Positional
   * parameters are represented by ? placeholders in the SQL.
   *
   * <p><b>Note:</b> either named or positional parameters can be used, but not both.</p>
   *
   * @param   parameters zero or more parameter values
   *
   * @return  this Query
   */
  public Update parameters(Object... parameters) {
    parameters(new HashMap<String,Object>() {{
      put(Parameters.POSITIONAL, parameters);
    }});
    return this;
  }

  /**
   * Fluent API to configure Query with named parameter values. Named
   * parameters are represented by :name placeholders in the SQL.
   *
   * <p><b>Note:</b> either named or positional parameters can be used, but not both.</p>
   *
   * @param   parameters map of parameter values by name
   *
   * @return  this Query
   */
  public Update parameters(Map<String,Object> parameters) {
    this.parameters = parameters;
    return this;
  }

  /**
   * Fluent API to configure Query with named parameter values. Named
   * parameters are represented by :name placeholders in the SQL.
   *
   * <p>This API allows the Result from one Query to be used as parameter values in another.</p>
   *
   * <p><b>Note:</b> either named or positional parameters can be used, but not both.</p>
   *
   * @param   result parameter values by name in a Result object
   *
   * @return  this Query
   */
  public Update parameters(Result result) {
    this.parameters = result.getAttributes();
    return this;
  }

}
