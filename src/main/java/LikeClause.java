import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class LikeClause {

    public void test()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_LikeClause");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            /*
            Wrong usage: "select name from things where name like '%:search%'"
            This query would try to select where name like '%:search%' literally, without binding any arguments.
            This is because JDBC drivers will not bind arguments inside string literals.
            It never gets that far, though this query will throw an exception, because we don’t allow unused argument bindings by default.

            The solution is to use SQL string concatenation:
             */
            List<String> names = handle.createQuery("select name from user where name like '%' || :search || '%'")
                    .bind("search", "B")
                    .mapTo(String.class)
                    .list();

            names.forEach(System.out::println);
        });
    }

    public static void main(String[] args) {
        LikeClause likeClause = new LikeClause();
        likeClause.test();
    }
}
