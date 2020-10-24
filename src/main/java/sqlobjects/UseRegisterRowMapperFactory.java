package sqlobjects;

import model.User;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class UseRegisterRowMapperFactory {
    public interface UserDao {
        @SqlUpdate("insert into users (id, name) values (?, ?)")
        int insert(long id, String name);

        @SqlUpdate("create table users (id int primary key, name varchar(100))")
        void createTable();

        @SqlQuery("select * from users")
        @RegisterRowMapperFactory(UserMapperFactory.class)
        List<User> list();

        class UserMapperFactory implements RowMapperFactory {

            public Optional<RowMapper<?>> build(Type type, ConfigRegistry config) {
                RowMapper<?> userMapper = (rs, ctx) ->
                        new User(rs.getInt("id"), rs.getString("name"));

                return Optional.of(userMapper);
            }
        }
    }

    public void testUserMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_testUserMapper");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        UseRegisterRowMapper.UserDao userDao = handle.attach(UseRegisterRowMapper.UserDao.class);

        userDao.createTable();
        userDao.insert(1, "Bob");
        userDao.insert(2, "Jan");

        List<User> users = userDao.list();
        users.forEach(user -> System.out.println(user));

        handle.close();
    }

    public static void main(String[] args) {
        UseRegisterRowMapperFactory factory = new UseRegisterRowMapperFactory();
        factory.testUserMapper();
    }
}
