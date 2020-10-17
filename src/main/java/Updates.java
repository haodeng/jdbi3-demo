import org.jdbi.v3.core.Jdbi;

/**
 * Updates are operations that return an integer number of rows modified,
 * such as a database INSERT, UPDATE, or DELETE.
 */
public class Updates {
    /**
     * You can execute a simple update with Handle's int execute(String sql, Object…​ args) method
     * which binds simple positional parameters.
     */
    public void byExecute()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_byExecute");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            handle.execute("update user set name = ? where id = ?", "BiB", 2);

            handle.select("select * from user")
                    .mapToMap()
                    .stream()
                    .forEach(map -> System.out.println(map));
        });
    }

    public void byCreateUpdate()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_byCreateUpdate");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            int count = handle.createUpdate("INSERT INTO user(id, name) VALUES(:id, :name)")
                    .bind("id", 3)
                    .bind("name", "Charlie")
                    .execute();
            System.out.println(count);

            count = handle.createUpdate("update user set name = :name where id = :id")
                    .bind("id", 3)
                    .bind("name", "Chars")
                    .execute();
            System.out.println(count);

            handle.select("select * from user")
                    .mapToMap()
                    .stream()
                    .forEach(map -> System.out.println(map));
        });
    }

    public static void main(String[] args) {
        Updates updates = new Updates();
        updates.byExecute();
        updates.byCreateUpdate();
    }
}
