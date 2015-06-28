package io.mflo.rxjava_sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscriber;

/**
 * Model SQL SELECT
 *
 * <p>A Query is created by the <code>SQL.query</code> method.
 * A fluent API configures the Query until it is ready to {@link #execute}.</p>
 *
 * @author      http://mflo.io
 * @version     0.0.1
 */

public final class Query implements Parameters {

  // logger for this class
  private static final Logger log = LoggerFactory.getLogger(Query.class);

  // private state
  private final String sql;
  private final DataSource ds;

  // state accumulated by fluent API
  // NOTE: execute should never modify these fields, so a Query can be reused
  private boolean allRows = false;
  private int[] limit = new int[] { 0, 1 };
  private OrderBy[] orderBys = new OrderBy[0];
  private Result parameters = new Result();
  private int queryTimeout = 0;

  /**
   * Private ctor: use <code>SQL.query</code>
   *
   * @param   sql SQL SELECT statement
   * @param   ds data source
   *
   * @see     SQL#query to construct from a string representation
   */
  protected Query(String sql,
                  DataSource ds) {
    this.sql = sql;
    this.ds = ds;
  }

  /**
   * Fluent API to configure Query to retrieve all matching rows
   *
   * @param   allRows true for all matching rows, false for only those in <code>limit</code>
   *
   * @return  this Query
   *
   * @see     #limit(int[])
   */
  public Query allRows(boolean allRows) {
    this.allRows = allRows;
    return this;
  }

  /**
   * Execute this <code>Query</code>
   *
   * @return  Observable results
   */
  public Observable<Result> execute() {
    return Observable.create((subscriber) -> {
      int[] window = new int[] { limit[0], limit[1] };
      do {
        try (Connection connection = ds.getConnection()) {
          Map<String,Integer> ordinalByName = new HashMap<>();
          String prepared = prepareStatement(window, ordinalByName);
          try (PreparedStatement stmt = connection.prepareStatement(prepared)) {
            stmt.setQueryTimeout(queryTimeout);
            injectParameters(stmt, parameters, ordinalByName);
            log.debug(stmt.toString());
            try (ResultSet rs = stmt.executeQuery()) {
              int count = 0;
              for (count = 0; rs.next() && !subscriber.isUnsubscribed(); count++)
                subscriber.onNext(new Result(populate(rs)));
              if (count < window[1])
                subscriber.unsubscribe();
            }
          }
        }
        catch (SQLException e) {
          subscriber.onError(e);
        }
        window[0] += window[1];
      } while (allRows && !subscriber.isUnsubscribed());
      subscriber.onCompleted();
    });
  }

  /**
   * Fluent API to configure Query to limit count of rows retrieved. If <code>limit</code>
   * is not called, only the first matching row is retrieved. If <code>allRows(true)</code>
   * is called, the number of rows is not limited but instead retrieval is windowed
   * according to the <code>limit</code> specification.
   *
   * @param   limit [0] indicates starting position
   *                [1] indicates maximum number of rows to retrieve
   *
   * @return  this Query
   *
   * @see     #allRows(boolean)
   */
  public Query limit(int[] limit) {
    this.limit = limit;
    return this;
  }

  /**
   * Fluent API to configure Query to limit count of rows retrieved.
   *
   * @param   start starting position
   * @param   count maximum number of rows to retrieve
   *
   * @return  this Query
   *
   * @see     #limit(int[])
   */
  public Query limit(int start,
                     int count) {
    this.limit = new int[] { start, count };
    return this;
  }

  /**
   * Fluent API to configure Query with retrieval order specification
   *
   * @param   orderBys zero or more OrderBy sopecifications
   *
   * @return  this Query
   */
  public Query orderBy(OrderBy... orderBys) {
    this.orderBys = orderBys;
    return this;
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
  public Query parameters(Object... parameters) {
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
  public Query parameters(Map<String,Object> parameters) {
    parameters(new Result(parameters));
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
   * @param   parameters parameter values by name in a Result object
   *
   * @return  this Query
   */
  public Query parameters(Result parameters) {
    this.parameters = parameters;
    return this;
  }

  /*
   * Populate a map of name/value pairs from a <code>ResultSet</code>.
   *
   * @param   rs
   *
   * @return  attributes map of name/value pairs
   *
   * @throws  SQLException
   */
  private Map<String,Object> populate(ResultSet rs) throws SQLException {
    Map<String,Object> attributes = new HashMap<>();
    ResultSetMetaData rsMetaData = rs.getMetaData();
    for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
      String identifier = rsMetaData.getColumnName(i);
      String alias = rsMetaData.getColumnLabel(i);
      if (alias != null)
        identifier = alias;
      attributes.put(identifier, rs.getString(i));
    }
    return attributes;
  }

  /*
   * Pre-prepare an SQL statement by adding LIMIT and ORDER BY clauses
   *
   * @param   window
   * @param   ordinalByName
   *
   * @return  sql
   */
  private String prepareStatement(int[] window,
                                  Map<String,Integer> ordinalByName) {
    StringBuilder prepared = new StringBuilder(sql);
    if (orderBys.length > 0)
      prepared.append(" ORDER BY " + OrderBy.join(orderBys));
    prepared.append(" LIMIT " + window[0] + "," + window[1]);
    return orderParameterNames(prepared.toString(), ordinalByName);
  }

  /**
   * Fluent API to configure Query with a timeout value in seconds, such that any
   * query that takes longer to execute than specified is canceled. If
   * <code>queryTimeout</code> is not called then no timeout is in effect.
   *
   * @param   queryTimeout timeout value in seconds; zero means no limit
   *
   * @return  this Query
   */
  public Query queryTimeout(int queryTimeout) {
    this.queryTimeout = queryTimeout;
    return this;
  }

}
