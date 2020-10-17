import model.User;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Batch;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Arrays;
import java.util.Collection;

public class Batches {
    /**
     * A Batch sends many commands to the server in bulk.
     *
     * After opening the batch, repeated add statements, and invoke add.
     */
    public void createBatch()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_createBatch");
        jdbi.useHandle(handle -> {
            handle.execute("create table fruit (id int primary key, name varchar(100))");
            Batch batch = handle.createBatch();

            batch.add("INSERT INTO fruit VALUES(0, 'apple')");
            batch.add("INSERT INTO fruit VALUES(1, 'banana')");

            int[] rowsModified = batch.execute();
            System.out.println(rowsModified.length + ": [" + rowsModified[0] + ", " + rowsModified[1] + "]");

            handle.select("select * from fruit")
                    .mapToMap()
                    .stream()
                    .forEach(map -> System.out.println(map));
        });
    }

    /**
     * A PreparedBatch sends one statement to the server with many argument sets.
     * The statement is executed repeatedly, once for each batch of arguments that is add-ed to it.
     *
     * The result is again a int[] of modified row count.
     */
    public void preparedBatch()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_preparedBatch");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");

            PreparedBatch batch = handle.prepareBatch("INSERT INTO user(id, name) VALUES(:id, :name)");
            for (int i = 0; i < 10; i++) {
                batch.bind("id", i)
                        .bind("name", "User:" + i)
                        .add();
            }
            int[] counts = batch.execute();
            System.out.println(counts.length);

            handle.select("select * from user")
                    .mapToMap()
                    .stream()
                    .forEach(map -> System.out.print(map + ","));
        });

        System.out.println();
    }

    public interface UserDAO {
        //SqlObject also supports batch inserts
        @SqlBatch("INSERT INTO user VALUES(:id, :name)")
        int[] addUsersInBatch(@BindBean Collection<User> users);

        @SqlQuery("SELECT count(1) FROM user")
        int count();
    }

    public void sqlObjectBatch()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_sqlObjectBatch");
        jdbi.installPlugin(new SqlObjectPlugin());

        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");

            UserDAO userDAO = handle.attach(UserDAO.class);

            int[] rowsModified = userDAO.addUsersInBatch(Arrays.asList(
                    new User(0, "Peter"),
                    new User(1, "Bill")));

            System.out.println(rowsModified.length);
            System.out.println(userDAO.count());
        });
    }

    public static void main(String[] args) {
        Batches batches = new Batches();
        batches.createBatch();
        batches.preparedBatch();
        batches.sqlObjectBatch();
    }
}
