import model.User;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

import java.util.List;

/**
 * An Update or PreparedBatch may automatically generate keys.
 * These keys are treated separately from normal results.
 * Depending on your database and configuration, the entire inserted row may be available.
 *
 * Unfortunately there is a lot of variation between databases supporting this feature
 * so please test this feature’s interaction with your database thoroughly.
 */
public class GeneratedKeys {
    public void generatedKeys()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_generatedKeys");
        jdbi.registerRowMapper(ConstructorMapper.factory(User.class));

        jdbi.useHandle(handle -> {
            handle.execute("create table users (id int primary key, name varchar(100))");

            //H2 do not support generatedKeys
            User user = handle.createUpdate("INSERT INTO users (name) VALUES(?)")
                    .bind(0, "Data")
                    .executeAndReturnGeneratedKeys()
                    .mapTo(User.class)
                    .one();

            System.out.println(user);
        });
    }

    public static void main(String[] args) {
        GeneratedKeys generatedKeys = new GeneratedKeys();
        generatedKeys.generatedKeys();
    }
}
