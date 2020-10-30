package sqlobjects;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Map;

public class UseRegisterFieldMapper {
    public interface UserDao {
        @SqlUpdate("insert into users (id, name, role_id) values (?, ?, ?)")
        int insert(long id, String name, long roleId);

        @SqlUpdate("create table users (id int primary key, name varchar(100), role_id int)")
        void createTable();

        @SqlQuery("select * from users")
        @RegisterFieldMapper(User.class)
        List<User> list();

        @SqlQuery("select u.id u_id, u.name u_name, r.id r_id, r.name r_name " +
                "from users u left join roles r on u.role_id = r.id")
        @RegisterFieldMapper(value = User.class, prefix = "u")
        @RegisterFieldMapper(value = Role.class, prefix = "r")
        Map<User,Role> getRolesPerUser();
    }

    public static class User {
        private int id;
        private String name;

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

    public static class Role {
        private int id;
        private String name;

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
            return "(Role id:" + id +
                    ", name:" + name + ")";
        }
    }

    public void testFieldMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_testFieldMapper");
        jdbi.installPlugin(new SqlObjectPlugin());

        try (Handle handle = jdbi.open()) {
            UserDao userDao = handle.attach(UserDao.class);

            userDao.createTable();
            userDao.insert(1L, "Bob", 1);
            userDao.insert(2L, "Kim", 2);

            userDao.list().forEach(user -> System.out.println(user));

            handle.execute("create table roles (id int primary key, name varchar(100))");
            handle.execute("insert into roles (id, name) values (1, 'guest')");
            handle.execute("insert into roles (id, name) values (2, 'admin')");

            Map<User, Role> userRoleMap = userDao.getRolesPerUser();
            userRoleMap.entrySet()
                    .forEach(entry -> System.out.println(entry.getKey() + ", " + entry.getValue()));
        }

    }

    public static void main(String[] args) {
        UseRegisterFieldMapper mapper = new UseRegisterFieldMapper();
        mapper.testFieldMapper();
    }
}
