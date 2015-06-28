package io.mflo.rxjava_sql;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initiate SQL operations against a <code>DataSource</code>.
 *
 * @author      http://mflo.io
 * @version     0.0.1
 */

public final class SQL {

  // logger for this class
  private static final Logger log = LoggerFactory.getLogger(SQL.class);

  // private state
  private final DataSource ds;

  /**
   * Construct an <code>SQL</code> from a <code>DataSource</code>
   *
   * @param   ds <code>DataSource</code>
   */
  public SQL(DataSource ds) {
    this.ds = ds;
  }

  /**
   * Create a <code>Batch</code> of SQL statements from a stream, like a Java resource
   *
   * @param   reader <code>Reader</code>
   *
   * @return  Batch to be configured by fluent API
   *
   * @throws  IOException if the stream can't be processed
   */
  public Batch batch(Reader reader) throws IOException {
    List<String> sqls = new ArrayList<>();
    try (BufferedReader buffered = new BufferedReader(reader)) {
      String line = null;
      while((line = buffered.readLine()) != null)
        if (!line.trim().isEmpty())
          sqls.add(line);
    }
    return batch(sqls);
  }

  /**
   * Create a <code>Batch</code> of SQL statements from a list
   *
   * @param   sqls in a list
   *
   * @return  <code>Batch</code> to be configured by fluent API
   */
  public Batch batch(List<String> sqls) {
    return new Batch(sqls, ds);
  }

  /**
   * Create a <code>Query</code> from an SQL SELECT statement
   *
   * @param   sql SELECT statement
   *
   * @return  <code>Query</code> to be configured by fluent API
   */
  public Query query(String sql) {
    return new Query(sql, ds);
  }

  /**
   * Create an <code>UYpdate</code> from an SQL DELETE/INSERT/UPDATE statement
   *
   * @param   sql DELETE/INSERT/UPDATE statement
   *
   * @return  <code>Update</code> to be configured by fluent API
   */
  public Update update(String sql) {
    return new Update(sql, ds);
  }

}
