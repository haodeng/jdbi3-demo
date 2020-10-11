package mapper;

import model.User;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class RowMappers {
    /**
     * RowMapper is a functional interface, which maps the current row of a JDBC ResultSet to a mapped type.
     * Row mappers are invoked once for each row in the result set.
     */
    public void rowMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_rowMapper");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            List<User> users = handle.createQuery("SELECT id, name FROM user ORDER BY id ASC")
                    .map((rs, ctx) -> new User(rs.getInt("id"), rs.getString("name")))
                    .list();

            System.out.println(users);
        });
    }

    class UserMapper implements RowMapper<User> {
        @Override
        public User map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new User(rs.getInt("id"), rs.getString("name"));
        }
    }

    /**
     * Row mappers may be defined as classes, which allows for re-use
     */
    public void rowMapperAsClass()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_rowMapperAsClass");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            List<User> users = handle.createQuery("SELECT id, name FROM user ORDER BY id ASC")
                    .map(new UserMapper())
                    .list();

            System.out.println(users);
        });
    }

    /**
     * Row mappers can be registered for particular types.
     * This simplifies usage, requiring only that you specify what type you want to map to.
     * Jdbi automatically looks up the mapper from the registry, and uses it.
     */
    public void rowMappersRegistry()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_rowMappersRegistry");
        jdbi.registerRowMapper(User.class,
                (rs, ctx) -> new User(rs.getInt("id"), rs.getString("name")));

        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            List<User> users = handle.createQuery("SELECT id, name FROM user ORDER BY id ASC")
                    .mapTo(User.class)
                    .list();

            System.out.println(users);
        });
    }

    public static void main(String[] args) {
        RowMappers mappers = new RowMappers();
        mappers.rowMapper();
        mappers.rowMapperAsClass();
        mappers.rowMappersRegistry();
    }
}


