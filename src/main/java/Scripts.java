import org.jdbi.v3.core.Jdbi;

/**
 * A Script parses a String into semicolon terminated statements.
 * The statements can be executed in a single Batch or individually.
 */
public class Scripts {

    public void scripts()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_callProcedure");
        jdbi.useHandle(handle -> {
            int[] results = handle.createScript(
                    "create table user (id int primary key, name varchar(100));" +
                    "INSERT INTO user VALUES(3, 'Charlie');" +
                            "UPDATE user SET name='Bobby Tables' WHERE id=3;")
                    .execute();
            System.out.println(results.length);

            handle.select("select * from user")
                    .mapToMap()
                    .stream()
                    .forEach(map -> System.out.println(map));
        });
    }

    public static void main(String[] args) {
        Scripts scripts = new Scripts();
        scripts.scripts();
    }
}
