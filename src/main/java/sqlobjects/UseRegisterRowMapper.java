package sqlobjects;

import model.User;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UseRegisterRowMapper {

    public interface UserDao {
        @SqlUpdate("insert into users (id, name) values (?, ?)")
        int insert(long id, String name);

        @SqlUpdate("create table users (id int primary key, name varchar(100))")
        void createTable();

        @SqlQuery("select * from users")
        @RegisterRowMapper(UserMapper.class)
        List<User> list();

        class UserMapper implements RowMapper<User> {
            @Override
            public User map(ResultSet rs, StatementContext ctx) throws SQLException {
                return new User(rs.getInt("id"), rs.getString("name"));
            }
        }
    }

    public void testUserMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_testUserMapper");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        UserDao userDao = handle.attach(UserDao.class);

        userDao.createTable();
        userDao.insert(1, "Bob");
        userDao.insert(2, "Jan");

        List<User> users = userDao.list();
        users.forEach(user -> System.out.println(user));

        handle.close();
    }

    public static void main(String[] args) {
        UseRegisterRowMapper mapper = new UseRegisterRowMapper();
        mapper.testUserMapper();
    }
}
