import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

public class CustomizedArguments {

    //The Argument interface wraps a single value into a binding.
    static class UUIDArgument implements Argument {
        private UUID uuid;

        UUIDArgument(UUID uuid) {
            this.uuid = uuid;
        }

        /*
        Since Argument usually directly calls into JDBC directly,
        it is given the one-based index (as expected by JDBC) when it is applied.
         */
        @Override
        public void apply(int position, PreparedStatement statement, StatementContext ctx)
                throws SQLException {
            statement.setString(position, uuid.toString());
        }
    }

    public void customArguments()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_customArguments");

        UUID u = UUID.randomUUID();
        String uuid = jdbi.withHandle(handle -> {
            handle.execute("create table uuidtest (uuid UUID)");

            handle.createUpdate("insert into uuidtest (uuid) values (?)")
                    .bind(0, "e5e59a7b-9aab-11ea-886c-0026b937cf5e")
                    .execute();

            return handle.createQuery("SELECT CAST(:uuid AS VARCHAR)")
                    .bind("uuid", new UUIDArgument(u))
                    .mapTo(String.class)
                    .one();
        });

        assert uuid.equals("e5e59a7b-9aab-11ea-886c-0026b937cf5e");
    }

    /**
     * The ArgumentFactory interface provides Argument instances for any data type it knows about.
     * By implementing and registering an argument factory,
     * it is possible to bind custom data types without having to explicitly wrap them in Argument objects.
     */
    static class UUIDArgumentFactory extends AbstractArgumentFactory<UUID> {
        UUIDArgumentFactory() {
            //The JDBC SQL type constant to use when binding UUIDs. Jdbi needs this in order to bind UUID values of null
            super(Types.VARCHAR);
        }

        @Override
        protected Argument build(UUID value, ConfigRegistry config) {
            return (position, statement, ctx) -> statement.setString(position, value.toString());
        }
    }

    public void argumentFactory()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_argumentFactory");
        UUID u = UUID.randomUUID();
        String uuid = jdbi.withHandle(handle -> {
            handle.registerArgument(new UUIDArgumentFactory());

            handle.execute("create table uuidtest (uuid UUID)");

            handle.createUpdate("insert into uuidtest (uuid) values (?)")
                    .bind(0, "e5e59a7b-9aab-11ea-886c-0026b937cf5e")
                    .execute();

            return handle.createQuery("SELECT CAST(:uuid AS VARCHAR)")
                    .bind("uuid", u)
                    .mapTo(String.class)
                    .one();
        });
        assert uuid.equals("e5e59a7b-9aab-11ea-886c-0026b937cf5e");
    }

    public static void main(String[] args) {
        CustomizedArguments demo = new CustomizedArguments();
        demo.customArguments();
        demo.argumentFactory();
    }
}
