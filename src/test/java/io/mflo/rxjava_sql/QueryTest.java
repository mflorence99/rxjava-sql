package io.mflo.rxjava_sql;

import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;

import org.junit.*;
import static org.junit.Assert.*;

import rx.Observable;
import rx.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for <code>Query</code>
 *
 * @author      http://mflo.io
 * @version     0.0.1
 */

public class QueryTest {

  private static final Logger log = LoggerFactory.getLogger(QueryTest.class);

  private DataSource ds;
  private SQL sql;

  @Before public void setUp() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
    properties.setProperty("url", "jdbc:mysql://localhost/test?user=root&password=beerhunter");
    ds = BasicDataSourceFactory.createDataSource(properties);
    sql = new SQL(ds);
    sql.batch(new InputStreamReader(getClass().getResourceAsStream("/testdata.sql"))).execute();
  }

  @Test public void testQuery() {
    sql.query("select first as x, last as y from person where title = :title")
      .parameters(new HashMap<String,Object>() {{
          put("title", "Cat");
        }})
      .orderBy(new OrderBy.DESC("x"))
      .allRows(false)
      .execute()
      .subscribe((result) -> {
          assertEquals("Query should find last cat in alpha order", "Max", result.get("x"));
        });
  }

  @Test public void testJoin() {
    sql.query("select description from title, person where person.first = ? and person.title = title.title")
      .parameters("Lucky")
      .execute()
      .subscribe((result) -> {
          assertEquals("Join should match title to description", "I am a cat", result.get("description"));
        });
  }

  @Test public void testChain() {
    sql.query("select * from person")
      .execute()
      .subscribe((r1) -> {
          sql.query("select * from title where title = :title")
            .parameters(r1)
            .execute()
            .subscribe((r2) -> {
                assertEquals("Chain should use result from one query as parameters in another", "I am a man", r2.get("description"));
              });
        });
  }

}
