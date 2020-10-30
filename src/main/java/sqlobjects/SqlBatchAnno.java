package sqlobjects;

import com.google.common.collect.ImmutableList;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
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
    public static class User {
        private int id;
        private String name;

        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return "(User id:" + id +
                    ", name:" + name + ")";
        }
    }


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

        /**
         * Constant values may also be used as parameters to a SQL batch.
         * In this case, the same value is bound to that parameter for every SQL statement in the batch.
         */
        @SqlBatch("insert into contacts (tenant_id, id, name) values (:tenantId, :user.id, :user.name)")
        void bulkInsert(@Bind("tenantId") long tenantId,
                        @BindBean("user") User... users);

        @SqlQuery("select name from users")
        List<String> listUserNames();

        @SqlQuery("select name from contacts")
        List<String> listContactNames();
    }

    public void test()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_test");
        jdbi.installPlugin(new SqlObjectPlugin());

        try (Handle handle = jdbi.open()) {
            UserDao userDao = handle.attach(UserDao.class);

            userDao.createTable();
            /**
             * This would execute:
             * insert into users (id, name, email) values (1, 'foo', 'a@example.com');
             * insert into users (id, name, email) values (2, 'bar', 'b@example.com');
             * insert into users (id, name, email) values (3, 'baz', 'c@fake.com');
             */
            userDao.bulkInsert(
                    ImmutableList.of(1, 2, 3),
                    ImmutableList.of("foo", "bar", "baz").iterator(),
                    "a@example.com", "b@example.com", "c@fake.com");

            System.out.println(userDao.listUserNames().size());
        }
    }

    public void test2()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_test2");
        jdbi.installPlugin(new SqlObjectPlugin());

        try (Handle handle = jdbi.open()) {
            UserDao userDao = handle.attach(UserDao.class);
            handle.execute("create table contacts (id int primary key, name varchar(100), tenant_id int)");

            User[] users = new User[]{
                    new User(1, "Bob"),
                    new User(2, "Jan")
            };
            /**
             * This would execute:
             * insert into contacts (id, name, tenant_id) values (1, 'Bob', 1);
             * insert into contacts (id, name, tenant_id) values (2, 'Jan', 1);
             */
            userDao.bulkInsert(1, users);

            System.out.println(userDao.listContactNames().size());
        }

    }
    public static void main(String[] args) {
        SqlBatchAnno sqlBatchAnno = new SqlBatchAnno();
        sqlBatchAnno.test();
        sqlBatchAnno.test2();
    }
}
