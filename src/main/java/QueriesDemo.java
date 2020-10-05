import org.jdbi.v3.core.Jdbi;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class QueriesDemo {

    public void getResultSet()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_getResultSet");

        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            List<Map<String, Object>> users = handle.createQuery("SELECT id, name FROM user ORDER BY id ASC")
                    .mapToMap()
                    .list();

            System.out.println(users);

            assert users.get(0).get("name").equals("Alice");
            assert users.get(1).get("name").equals("Bob");
        });
    }

    /**
     * Call one() when you expect the result to contain exactly one row.
     * Returns null only if the returned row maps to null.
     * Throws an exception if the result has zero or multiple rows
     */
    public void exactlyOneRow()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_exactlyOneRow");

        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            String user = handle.select("select name from user where id = ?", 1)
                    .mapTo(String.class)
                    .one();

            System.out.println(user);
            assert user.equals("Alice");

            //Throws an exception if the result has zero
            try
            {
                String userNotExistExpectException = handle.select("select name from user where id = ?", 9)
                        .mapTo(String.class)
                        .one();
            }
            catch (IllegalStateException e)
            {
                System.out.println("Exception expected, user not found. " + e.getMessage());
            }
        });
    }

    /**
     * Call findOne() when you expect the result to contain zero or one row.
     * Returns Optional.empty() if there are no rows, or one row that maps to null.
     * Throws an exception if the result has multiple rows.
     */
    public void findOne()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_findOne");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            Optional<String> name = handle.select("select name from user where id = ?", 1)
                    .mapTo(String.class)
                    .findOne();

            System.out.println(name.get());
            assert name.get().equals("Alice");
        });
    }

    /**
     * Call first() when you expect the result to contain at least one row.
     * Returns null if the first row maps to null.
     * Throws an exception if the result has zero rows.
     */
    public void first()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_first");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            String name = handle.select("select name from user where id = ?", 1)
                    .mapTo(String.class)
                    .first();

            System.out.println(name);
            assert name.equals("Alice");
        });
    }

    /**
     * Call findFirst() when the result may contain any number of rows.
     * Returns Optional.empty() if there are no rows, or the first row maps to null.
     */
    public void findFirst()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_findFirst");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            //Select more than one row
            Optional<String> oneName = handle.select("select name from user order by id")
                    .mapTo(String.class)
                    .findFirst();

            System.out.println(oneName.get());
            assert oneName.get().equals("Alice");
        });
    }

    /**
     * Multiple result rows can be returned in a list
     */
    public void multipleResultRowsReturnInList()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_multipleResultRowsReturnInList");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            List<String> names = handle.createQuery(
                    "select name from user")
                    .mapTo(String.class)
                    .list();

            System.out.println(names);
            assert names.size() == 2;
        });
    }

    /**
     * For other collections, use collect() with a collector
     */
    public void collectAsSet()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_collectAsSet");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            Set<String> names = handle.createQuery(
                    "select name from user")
                    .mapTo(String.class)
                    .collect(Collectors.toSet());

            System.out.println(names);
            assert names.size() == 2;
        });
    }

    public void streamResults()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_streamResults");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            handle.createQuery(
                    "select name from user")
                    .mapTo(String.class)
                    .useStream(stream -> {
                        stream.forEach(name -> System.out.println(name));
                    });

            handle.createQuery(
                    "select name from user")
                    .mapTo(String.class)
                    .stream()
                    .forEach(name -> System.out.println(name));
        });
    }

    public void mapToOtherDataTypes()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_mapToOtherDataTypes");
        jdbi.useHandle(handle -> {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            handle.execute("create table user (id int primary key, name varchar(100), started timestamp)");
            handle.execute("insert into user (id, name, started) values (?, ?, ?)", 1, "Alice", now);
            handle.execute("insert into user (id, name, started) values (?, ?, ?)", 2, "Bob", now);

            LocalDate releaseDate = handle.createQuery(
                    "select started from user where name = :name")
                    .bind("name", "Alice")
                    .mapTo(LocalDate.class)
                    .one();

            System.out.println(releaseDate);
        });
    }

    public static void main(String[] args) {
        QueriesDemo demo = new QueriesDemo();

        demo.getResultSet();
        demo.exactlyOneRow();
        demo.findOne();
        demo.first();
        demo.findFirst();

        demo.multipleResultRowsReturnInList();
        demo.collectAsSet();
        demo.streamResults();

        demo.mapToOtherDataTypes();
    }
}
