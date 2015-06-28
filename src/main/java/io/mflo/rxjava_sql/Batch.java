package io.mflo.rxjava_sql;

import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model SQL batch or bulk operations
 *
 * <p>A Batch is created by the <code>SQL.batch</code> methods.
 * A fluent API configures the Batch until it is ready to {@link #execute}.</p>
 *
 * <p><b>Note:</b> only the <code>execute()</code> method is currently
 * defined for this class.</p>
 *
 * @author      http://mflo.io
 * @version     0.0.1
 */

public final class Batch {

  // logger for this class
  private static final Logger log = LoggerFactory.getLogger(Batch.class);

  // private state
  private final List<String> sqls;
  private final DataSource ds;

  /**
   * Private ctor: use <code>SQL.batch</code>
   *
   * @param   sqls list of SQL statements
   * @param   ds data source
   *
   * @see     SQL#batch(java.util.List) to construct from a list of SQL statements
   * @see     SQL#batch(java.io.Reader) to construct from SQL statements in an external stream
   */
  protected Batch(List<String> sqls,
                  DataSource ds) {
    this.sqls = sqls;
    this.ds = ds;
  }

  /**
   * Execute this <code>Batch</code> of SQL
   *
   * @return  count number of rows affected
   *
   * @throws  SQLException when any SQL statement fails
   */
  public int execute() throws SQLException {
    int count = 0;
    try (Connection connection = ds.getConnection()) {
      for (String sql : sqls) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
          log.debug(stmt.toString());
          count += stmt.executeUpdate();
        }
      }
    }
    return count;
  }

}
