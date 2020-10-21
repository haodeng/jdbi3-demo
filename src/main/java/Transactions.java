import model.User;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

import java.util.Optional;

/**
 * jdbi provides full support for JDBC transactions.
 *
 * Handle objects provide two ways to open a transaction
 * inTransaction allows you to return a result, and useTransaction has no return value.
 */
public class Transactions {

    public void inTransaction()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_inTransaction");

        //inTransaction allows you to return a result
        jdbi.useHandle(handle -> {
            handle.registerRowMapper(ConstructorMapper.factory(User.class));

            handle.execute("create table user (id int primary key, name varchar(100))");

            Optional<User> user = handle.inTransaction(h -> {
                h.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
                h.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

                return h.createQuery("SELECT * FROM user WHERE id=:id")
                        .bind("id", 1)
                        .mapTo(User.class)
                        .findFirst();
            });

            System.out.println(user.get());
        });
    }

    public void useTransaction()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_useTransaction");

        //useTransaction has no return value
        jdbi.useHandle(handle -> {
            handle.registerRowMapper(ConstructorMapper.factory(User.class));

            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.useTransaction(h ->
            {
                h.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
                h.execute("insert into user (id, name) values (?, ?)", 2, "Bob");
            });

            Optional<User> user = handle.createQuery("SELECT * FROM user WHERE id=:id")
                    .bind("id", 1)
                    .mapTo(User.class)
                    .findFirst();

            System.out.println(user.get());
        });
    }

    public void commitRollback()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_commitRollback");

        jdbi.useHandle(handle -> {
            handle.registerRowMapper(ConstructorMapper.factory(User.class));

            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.useTransaction(h ->
            {
                h.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
                h.rollback();
                h.execute("insert into user (id, name) values (?, ?)", 2, "Bob");
                h.commit();
            });

            Optional<User> user1 = handle.createQuery("SELECT * FROM user WHERE id=:id")
                    .bind("id", 1)
                    .mapTo(User.class)
                    .findFirst();
            //user 1 is rolled back, no value
            System.out.println(user1.isPresent());

            Optional<User> user2 = handle.createQuery("SELECT * FROM user WHERE id=:id")
                    .bind("id", 2)
                    .mapTo(User.class)
                    .findFirst();
            //user 2 is committed
            System.out.println(user2.isPresent() + ", " + user2.get());
        });
    }

    public static void main(String[] args) {
        Transactions t = new Transactions();
        t.inTransaction();
        t.useTransaction();

        t.commitRollback();
    }
}
