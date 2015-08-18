package io.mflo.rxjava_sql;

import java.lang.StringBuffer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support the <code>parameters</code> fluent API on the various
 * SQL statement builder classes.
 *
 * @author      http://mflo.io
 * @version     0.0.1
 */

public interface Parameters {

  /** Parameter map key to indicate a list of positional parameters */
  public static final String POSITIONAL = "positional";

  /**
   * Inject parameter values to placeholder tokens in the SQL statement.
   * Parameters can be named (:name) or positional (?).
   *
   * @param   stmt SQL statement
   * @param   parameters to be injected
   * @param   ordinalByName map of parameter positions by parameter name
   *
   * @throws  SQLException when parameter injection fails
   */
  public default void injectParameters(PreparedStatement stmt,
                                       Map<String,Object> parameters,
                                       Map<String,Integer> ordinalByName)
                      throws SQLException {
    Object[] params = (Object[])parameters.get(POSITIONAL);
    if (params != null)
      for (int i = 0; i < params.length; i++)
        stmt.setObject(i + 1, params[i]);
    else for (String name : ordinalByName.keySet()) {
      int ordinal = ordinalByName.get(name);
      stmt.setObject(ordinal, parameters.get(name));
    }
  }

  /**
   * Finds the position (relative to 1) of each named parameter in the SQL statement.
   *
   * @param   sql SQL statement
   * @param   ordinalByName map of parameter positions to be populated
   *
   * @return  SQL statement with named parameters replaced by ?
   */
  public default String orderParameterNames(String sql,
                                            Map<String,Integer> ordinalByName) {
    Pattern p = Pattern.compile("(\\:[^ ]+)");
    Matcher m = p.matcher(sql);
    StringBuffer sb = new StringBuffer();
    ordinalByName.clear();
    for (int i = 1; m.find(); i++) {
      ordinalByName.put(m.group(1).substring(1), i);
      m.appendReplacement(sb, "?");
    }
    m.appendTail(sb);
    return sb.toString();
  }

}
