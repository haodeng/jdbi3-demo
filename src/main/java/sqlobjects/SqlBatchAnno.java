package sqlobjects;

import com.google.common.collect.ImmutableList;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Iterator;
import java.util.List;

/**
 * Use the @SqlBatch annotation for bulk update operations.
 * @SqlBatch is analogous to PreparedBatch in Core.
 */
public class SqlBatchAnno {

    public interface UserDao {

        @SqlUpdate("create table users (id int primary key, name varchar(100), email varchar(100))")
        void createTable();

        /**
         * Batch parameters may be collections, iterables, iterators, arrays (including varargs).
         * We’ll call these "iterables" for brevity.
         *
         * When a batch method is called, SQL Object iterates through the method’s iterable parameters,
         * and executes the SQL statement with the corresponding elements from each parameter.
         */
        @SqlBatch("insert into users (id, name, email) values (?, ?, ?)")
        void bulkInsert(List<Integer> ids,
                        Iterator<String> names,
                        String... emails);

        @SqlQuery("select name from users")
        List<String> listNames();
    }

    public void test()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_test");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        UserDao userDao = handle.attach(UserDao.class);

        userDao.createTable();
        userDao.bulkInsert(
                ImmutableList.of(1, 2, 3),
                ImmutableList.of("foo", "bar", "baz").iterator(),
                "a@example.com", "b@example.com", "c@fake.com");

        System.out.println(userDao.listNames().size());
    }

    public static void main(String[] args) {
        SqlBatchAnno sqlBatchAnno = new SqlBatchAnno();
        sqlBatchAnno.test();
    }
}
