import org.jdbi.v3.core.Jdbi;

import java.util.Arrays;
import java.util.List;

public class HandlerDemo
{
    /**
     * If your operation does not need to return a result, use Jdbi.useHandle(HandleConsumer):
     */
    public void useHandle()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_useHandle");
        jdbi.useHandle(handle ->
            handle.execute("create table contacts (id int primary key, name varchar(100))"));
    }

    /**
     * If your operation will return some result, use jdbi.withHandle():
     * @return
     */
    public List<String> withHandle()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_withHandle");
        List<String> names = jdbi.withHandle(handle -> {
            handle.execute("create table contacts (id int primary key, name varchar(100))");
            handle.execute("insert into contacts (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into contacts (id, name) values (?, ?)", 2, "Bob");

            return handle.createQuery("select name from contacts")
                    .mapTo(String.class)
                    .list();
        });

        assert names.containsAll(Arrays.asList("Alice", "Bob"));
        return names;
    }

    public static void main(String[] args) {
        HandlerDemo handlerDemo = new HandlerDemo();
        handlerDemo.useHandle();
        List<String> names = handlerDemo.withHandle();
        names.stream().forEach(n -> System.out.println(n));
    }
}
