package sqlobjects;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;
import org.jdbi.v3.core.mapper.ColumnMappers;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class UseRegisterColumnMapperFactory {

    public interface UserDao {
        @SqlUpdate("insert into users (id, name) values (?, ?)")
        int insert(long id, String name);

        @SqlUpdate("create table users (id int primary key, name varchar(100))")
        void createTable();

        @SqlQuery("select name from users")
        @RegisterColumnMapperFactory(MoneyMapperFactory.class)
        List<String> listUserNames();

        class MoneyMapperFactory implements ColumnMapperFactory {

            public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
                if (!Optional.class.equals(GenericTypes.getErasedType(type))) {
                    return Optional.empty();
                }

                Type t = GenericTypes.resolveType(Optional.class.getTypeParameters()[0], type);

                ColumnMapper<?> tMapper = config.get(ColumnMappers.class)
                        .findFor(t)
                        .orElseThrow(() -> new NoSuchMapperException(
                                "No column mapper registered for parameter " + t + " of type " + type));

                ColumnMapper<?> optionalMapper = (rs, col, ctx) ->
                        Optional.ofNullable(tMapper.map(rs, col, ctx));

                return Optional.of(optionalMapper);
            }
        }
    }

    public void testColumnMapperFactory() {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_testColumnMapperFactory");
        jdbi.installPlugin(new SqlObjectPlugin());

        try (Handle handle = jdbi.open()) {
            UserDao userDao = handle.attach(UserDao.class);

            userDao.createTable();
            userDao.insert(1L, "Bob");
            userDao.insert(2L, "Kim");

            userDao.listUserNames().forEach(name -> System.out.println(name));
        }
    }

    public static void main(String[] args) {
        UseRegisterColumnMapperFactory factory = new UseRegisterColumnMapperFactory();
        factory.testColumnMapperFactory();
    }
}
