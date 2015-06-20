package info.mflo.rxjava_sql;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model SQL order by clause
 *
 * @author      https://github.com/mflorence99/mflorence99.github.io
 * @version     0.0.1
 */

abstract public class OrderBy {

  private static final Logger log = LoggerFactory.getLogger(OrderBy.class);

  private boolean ascending;
  private String id;

  /**
   * Private ctor -- use inner ASC or DESC classes
   *
   * @param id column identifier
   * @param ascending true for ascending, false for descending
   */
  protected OrderBy(String id,
                    boolean ascending) {
    this.ascending = ascending;
    this.id = id;
  }

  /**
   * Access order by clause identifier
   *
   * @return  id
   */
  public String getID() {
    return id;
  }

  /**
   * Access order by clause sequence
   *
   * @return  true if ascending
   */
  public boolean isAscending() {
    return ascending;
  }

  /**
   * Join multiple order by clauses
   *
   * @param   orderBys array
   *
   * @return  clauses, separated by commas
   */
  public static String join(OrderBy[] orderBys) {
    return Arrays.toString(orderBys).replace("[", "").replace("]", "");
  }

  /**
   * Convert order by clause to an SQL string
   *
   * @return  SQL string
   */
  public String toString() {
    return getID() + " " + (isAscending()? "ASC" : "DESC");
  }

  /**
   * Create order by clause in ascending order
   */
  public static class ASC extends OrderBy {
    public ASC(String id) {
      super(id, true);
    }
  }

  /**
   * Create order by clause in descending order
   */
  public static class DESC extends OrderBy {
    public DESC(String id) {
      super(id, false);
    }
  }

}
