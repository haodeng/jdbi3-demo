import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Map;

public class QueriesDemo {

    public void getResultSet()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_getResultSet");

        List<Map<String, Object>> users = jdbi.withHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            return handle.createQuery("SELECT id, name FROM user ORDER BY id ASC")
                    .mapToMap()
                    .list();
        });

        System.out.println(users);

        assert users.get(0).get("name").equals("Alice");
        assert users.get(1).get("name").equals("Bob");
    }

    public void exactlyOneRow()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_exactlyOneRow");

        String user = jdbi.withHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            return handle.select("select name from user where id = ?", 1)
                    .mapTo(String.class)
                    .one();
        });

        System.out.println(user);
        assert user.equals("Alice");
    }

    public static void main(String[] args) {
        QueriesDemo demo = new QueriesDemo();

        demo.getResultSet();
        demo.exactlyOneRow();
    }
}
