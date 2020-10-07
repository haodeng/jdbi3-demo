import model.User;
import org.jdbi.v3.core.Jdbi;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Jdbi provides a simple constructor mapper
 * which uses reflection to assign columns to constructor parameters by name.
 */
public class ConstructorMapper {

    public void constructorMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_rowMapper");
        jdbi.useHandle(handle -> {
            //The constructor parameter names "id", "name" match the database column names
            handle.registerRowMapper(org.jdbi.v3.core.mapper.reflect.ConstructorMapper.factory(User.class));

            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            Set<User> userSet = handle.createQuery("SELECT * FROM user ORDER BY id ASC")
                    .mapTo(User.class)
                    .collect(Collectors.toSet());

            System.out.println(userSet);
        });
    }

    public static void main(String[] args) {
        ConstructorMapper mapper = new ConstructorMapper();
        mapper.constructorMapper();
    }
}
