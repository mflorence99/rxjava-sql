# ```rxjava-sql``` Simple Application of FRP to SQL

```rxjava-sql``` is very much in pre-release status and no artifacts have been released yet,
except for [preliminary Javadocs](https://mflorence99.github.io/rxjava-sql-docs/javadoc/index.html).
 
 
## Motivation

I became very interested in functional reactive programming and decided to experiment by 
building a library to functionally compose SQL database calls using JDBC and RxJava. Of course,
I immediately discovered that [David Moten's rxjava-jdbc](https://github.com/davidmoten/rxjava-jdbc) -
a most excellent and complete implementation - already existed.

Nonetheless, I wanted to learn and based on many years of using JDBC the old-fashioned way I had
some different directions - mostly inspired by 
[YAGNI](https://en.wikipedia.org/wiki/You_aren%27t_gonna_need_it) and 
[CQRS](http://martinfowler.com/bliki/CQRS.html) - which I wanted to take an API.


## Test Dataset

I'll draw some usage examples from this test dataset:

```SQL
  create table person(first varchar(255), last varchar(255), title varchar(255));
  
  insert into person values('Mark', 'Florence', 'Mr');
  insert into person values('Lucky', 'Florence', 'Cat');
  insert into person values('Lynn', 'Hendrickson', 'Ms');
  insert into person values('Max', 'Hendrickson', 'Cat');
  
  create table title(title varchar(255), description varchar(255));
  
  insert into title values('Cat', 'I am a cat');
  insert into title values('Mr', 'I am a man');
  insert into title values('Ms', 'I am a woman');
```


## Simple Use Case

We wish to execute a typical ad-hoc query in the functional style:

```Java
  SQL sql = new SQL( /* some data source */ );
  ...
  sql.query("select description from title, person" + 
            " where person.first = ? and person.last = ?" + 
            " and person.title = title.title")
    .parameters("Lucky", "Florence")
    .execute()
    .map( /* arbitrary RxJava operators*/ )
    .subscribe((result) -> { /* process result(s) */ });
```

Parameters can be named as an alternative to standard positional notation:

```Java
  sql.query("select description from title, person" + 
            " where person.first = :first and person.last = :last" + 
            " and person.title = title.title")
    .parameters(new HashMap<String,Object>() {{
        put("first", "Lucky");
        put("last", "Florence");
      }})
```

But positional and named parameters cannot both appear in the same query.


## Obtaining a ```DataSource```

There's only one way to establish the context for an SQL operation in ```rxjava-sql```
and that's through a ```DataSource```. 
This example uses [Apache Commons DBCP](https://commons.apache.org/proper/commons-dbcp/):

```Java
  import java.util.Properties;
  import javax.sql.DataSource;
  import org.apache.commons.dbcp2.BasicDataSourceFactory;
  ...
  Properties properties = new Properties();
  properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
  properties.setProperty("url", "jdbc:mysql://localhost/test?user=...&password=...");
  DataSource ds = BasicDataSourceFactory.createDataSource(properties);
  ...
  new SQL(ds).query(...).execute().subscribe(...);
```

A ```DataSource``` is usually easily obtainable from a host context like Tomcat.

> TODO: use [Guice](https://github.com/google/guice) for dependency injection.


## Query Configuration

In addition to the ```parameters``` function shown above, several fluent APIs are defined
to support configuration of the query before ```execute()``` is called:

```Java
  sql.query("select first as x, last as y from person where title = :title")
    .parameters(new HashMap<String,Object>() {{
        put("title", "Cat");
      }})
    .orderBy(new OrderBy.ASC("x"), new OrderBy.DESC("y"))
    .limit(0, 50)
    .queryTimeout(10)
    .allRows(false)
    .execute()
    .subscribe((result) -> { ... });
```


### ```orderBy``` API

Code as many ```OrderBy``` specifications as necessary, either individually or as a Java array.

> I separated ```orderBy``` from the query itself as it is commonly constructed from
signals from the UI.


### ```limit``` API

In order to perform efficient queries, it is often necessary the limit the range of the
query at the database layer. For example, a UI might show discrete pages of information, 100
rows at a time. Similarly, an 'infinite scroll' UI might fetch incremental data 100 results
at a time.

```limit``` specifies a start position and a maximum count of results to be retrieved, either as
individual parameters or a Java ```int[]``` array.

If ```limit``` is not called, *only the first matching result is returned*.

A UI like the ones described above can be supported by appropriately incrementing the
start position to 'window' the results.

> Similarly ```limit``` is separated from the query itself to allow it be be constructed from
UI signals.


### ```queryTimeout``` API

Specify in seconds the maximum time allowed for the query. Zero (or omitting the call
altogether) means no limit. When the timeout expires, the subscriber's ```onError```
handler is invoked and no results are returned.


### ```allRows``` API

Often of course it is necessary to traverse all the rows in the database that satisfy the
query but this can rarely be performed naively by requesting them all at once.

This can be achieved by calling ```allRows(true)```. In this case, a separate database
connection and JDBC ```ResultSet``` will be used to 'window' through all the results in a 
way that is completely transparent to the downstream subscribers.

The window used is defined by the ```limit``` call. Strictly speaking, no such call is required
but if you use ```allRows(true)``` without calling ```limit``` each ```ResultSet``` will
only contain one item. To better use database resources, select ```limit``` parameters that 
balance a smaller number of connections with a ```ResultSet``` size that doesn't risk
exhausting available memory.


## Result Model

Many if not most similar libraries (including Moten's ```rxjava-jdbc```) use some kind of
ORM to map query results to Java classes.