package sqlobjects;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Map;

public class GetGeneratedKeysAnno {
    public static class User {
        private int id;
        private String name;

        public User(){}

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

    /**
     * The @GetGeneratedKeys annotation may be used on a @SqlUpdate or @SqlBatch method
     * to return the keys generated from the SQL statement
     */
    public interface UserDao {
        @SqlUpdate("create table users (id int AUTO_INCREMENT primary key, name varchar(100))")
        void createTable();

        @SqlBatch("INSERT INTO users (name) VALUES(?)")
        @GetGeneratedKeys
        @RegisterBeanMapper(User.class)
        List<User> createUsers(String... names);
    }

    public void test() {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:GetGeneratedKeysAnno_test");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        UserDao userDao = handle.attach(UserDao.class);

        userDao.createTable();
        List<User> users = userDao.createUsers("Alice", "Bob", "Charlie");
        users.forEach(System.out::println);

        List<Map<String, Object>> usersMap = handle.select("select * from users").mapToMap().list();
        usersMap.stream().forEach(entry -> System.out.println(entry));
    }

    public static void main(String[] args) {
        GetGeneratedKeysAnno test = new GetGeneratedKeysAnno();
        test.test();
    }
}
