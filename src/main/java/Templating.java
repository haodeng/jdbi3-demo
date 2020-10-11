import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Map;

public class Templating {
    /**
     * Query templating is a common attack vector!
     * Always prefer binding parameters to static SQL over dynamic SQL when possible.
     */
    public void templating()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_templating");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            handle.execute("create table contact (id int primary key, name varchar(100))");
            handle.execute("insert into contact (id, name) values (?, ?)", 1, "Robin");
            handle.execute("insert into contact (id, name) values (?, ?)", 2, "Mikkel");

            List<Map<String, Object>> users = handle.createQuery("select * from <TABLE> where name = :n")
                    // -> "select * from user where name = :n"
                    .define("TABLE", "user")
                    // -> "select * from user where name = 'Bob'"
                    .bind("n", "Bob")
                    .mapToMap().list();
            System.out.println(users);

            //Bind attributes
            String paramName = "name";
            users = handle.createQuery("select * from <TABLE> where name = :<attr>")
                    // -> "select * from contact where name = :<attr>"
                    .define("TABLE", "contact")
                    // -> "select * from contact where name = :paramName"
                    .define("attr", paramName)
                    // -> "select * from contact where name = 'Robin'"
                    .bind(paramName, "Robin")
                    .mapToMap().list();
            System.out.println(users);
        });
    }

    public static void main(String[] args) {
        Templating templating = new Templating();
        templating.templating();
    }
}
