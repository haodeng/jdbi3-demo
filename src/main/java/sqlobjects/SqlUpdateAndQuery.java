package sqlobjects;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.result.ResultIterator;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SqlUpdateAndQuery {

    public interface UserDao {
        /**
         * By default, a @SqlUpdate method may return a handful of types:
         *
         * void: returns nothing (obviously)
         *
         * int or long: returns the update count. Depending on the database vendor and JDBC driver,
         * this may be either the number of rows changed,
         * or the number matched by the query (regardless of whether any data was changed).
         *
         * boolean: returns true if the update count is greater than zero.
         * @param id
         * @param name
         * @return
         */
        @SqlUpdate("insert into users (id, name) values (?, ?)")
        int insert(long id, String name);

        /**
         * @SqlUpdate can also be used for DDL (Data Definition Language) operations like creating or altering tables.
         */
        @SqlUpdate("create table users (id int primary key, name varchar(100))")
        void createTable();

        //When a multi-row method returns an empty result set, an empty collection is returned.
        @SqlQuery("select name from users")
        List<String> listNames();

        /**
         * If a single-row method returns multiple rows from the query,
         * only the first row in the result set is returned from the method.
         *
         * If a single-row method returns an empty result set, null is returned.
         * @param id
         * @return
         */
        @SqlQuery("select name from users where id = ?")
        String getName(long id);

        /**
         * Methods may return Optional values.
         * If the query returns no rows (or if the value in the row is null),
         * Optional.empty() is returned instead of null.
         *
         * SQL Object throws an exception if query returns more than one row.
         * @param id
         * @return
         */
        @SqlQuery("select name from users where id = ?")
        Optional<String> findName(long id);

        @SqlQuery("select name from users")
        ResultIterable<String> getNamesAsIterable();

        @SqlQuery("select name from users")
        ResultIterator<String> getNamesAsIterator();

        @SqlQuery("select name from users")
        Stream<String> getNamesAsStream();
    }

    public void test()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:testSqlUpdate");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        UserDao userDao = handle.attach(UserDao.class);

        userDao.createTable();
        userDao.insert(1, "Bob");
        userDao.insert(2, "Jan");

        System.out.println(userDao.getName(1));
        System.out.println(userDao.findName(10)); //Expect Optional.empty()
        System.out.println(userDao.listNames().size());

        handle.close();
    }

    /**
     * The objects returned from these methods hold database resources that must be explicitly closed
     * when you are done with them.
     *
     * We strongly recommend the use of try-with-resource blocks when calling these methods, to prevent resource leaks
     */
    public void interationOrStream() {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_interationOrStream");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        UserDao userDao = handle.attach(UserDao.class);

        userDao.createTable();
        userDao.insert(1, "Bob");
        userDao.insert(2, "Jan");

        ResultIterable<String> names1 = userDao.getNamesAsIterable();
        names1.stream().forEach(name -> System.out.println(name));

        try (ResultIterator<String> names2 = userDao.getNamesAsIterator()) {
            while (names2.hasNext())
            {
                System.out.println(names2.next());
            }
        }

        try (Stream<String> names3 = userDao.getNamesAsStream()) {
            names3.forEach(name -> System.out.println(name));
        }
    }

    public static void main(String[] args) {
        SqlUpdateAndQuery sqlUpdateAndQuery = new SqlUpdateAndQuery();
        sqlUpdateAndQuery.test();

        sqlUpdateAndQuery.interationOrStream();
    }
}
