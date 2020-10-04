import model.Contact;
import org.jdbi.v3.core.Jdbi;

import java.util.*;

public class ArgumentsDemo {

    /**
     * When a SQL statement uses ? tokens,
     * Jdbi can bind a values to parameters at the corresponding index (0-based)
     */
    public void positionalArguments()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_positionalArguments");
        String name = jdbi.withHandle(handle -> {
            handle.execute("create table contacts (id int primary key, name varchar(100))");

            handle.createUpdate("insert into contacts (id, name) values (?, ?)")
                    .bind(0, 3)
                    .bind(1, "Chuck")
                    .execute();

            return handle.createQuery("select name from contacts where id = ?")
                    .bind(0, 3)
                    .mapTo(String.class)
                    .one();
        });

        assert name.equals("Chuck");
    }

    /**
     * When a SQL statement uses colon-prefixed tokens like :name, Jdbi can bind parameters by name
     * Mixing named and positional arguments is not allowed, as it would become confusing very quickly.
     */
    public void namedArguments()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_namedArguments");
        String name = jdbi.withHandle(handle -> {
            handle.execute("create table contacts (id int primary key, name varchar(100))");

            //bind individual arguments, .bind(field, value)
            handle.createUpdate("insert into contacts (id, name) values (:id, :name)")
                    .bind("id", 3)
                    .bind("name", "Chuck")
                    .execute();

            return handle.createQuery("select name from contacts where id = :id")
                    .bind("id", 3)
                    .mapTo(String.class)
                    .one();
        });

        assert name.equals("Chuck");
    }

    /**
     * You can bind multiple arguments at once from the entries of a Map
     */
    public void bindMultipleArgumentsByMap()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_bindMultipleArgumentsByMap");

        Map<String, Object> contact = new HashMap<>();
        contact.put("id", 2);
        contact.put("name", "Bob");

        String name = jdbi.withHandle(handle -> {
            handle.execute("create table contacts (id int primary key, name varchar(100))");

            //bind by map
            handle.createUpdate("insert into contacts (id, name) values (:id, :name)")
                    .bindMap(contact)
                    .execute();

            return handle.createQuery("select name from contacts where id = :id")
                    .bind("id", 2)
                    .mapTo(String.class)
                    .one();
        });

        assert name.equals("Bob");
    }

    /**
     * You can bind multiple values at once, from either a List<T> or a vararg
     */
    public void bindMultipleValuesatOnce()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_bindMultipleValuesatOnce");

        List<String> nameList = new ArrayList<>();
        nameList.add("hao");
        nameList.add("hau");

        List<String> names = jdbi.withHandle(handle -> {
            handle.execute("create table contacts (id int primary key, name varchar(100))");
            handle.execute("insert into contacts (id, name) values (?, ?)", 1, "hao");
            handle.execute("insert into contacts (id, name) values (?, ?)", 2, "hau");

            //from a List<T>
            return handle.createQuery("SELECT name FROM contacts WHERE name in (<listOfNames>)")
                    .bindList("listOfNames", nameList)
                    .mapTo(String.class)
                    .list();
        });
        assert names.containsAll(Arrays.asList("hao", "hau"));

        names = jdbi.withHandle(handle -> {
            handle.execute("create table contacts (id int primary key, name varchar(100))");
            handle.execute("insert into contacts (id, name) values (?, ?)", 1, "hao");
            handle.execute("insert into contacts (id, name) values (?, ?)", 2, "hau");

            // Or, using the 'vararg' definition
            return handle.createQuery("SELECT name FROM contacts WHERE name in (<varargListOfNames>)")
                            .bindList("varargListOfNames", "hao", "hau")
                            .mapTo(String.class)
                            .list();
        });
        assert names.containsAll(Arrays.asList("hao", "hau"));
    }

    /**
     * You can bind multiple arguments from properties of a Java Bean
     */
    public void bindMultipleArgumentsViaBean()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_bindMultipleArgumentsViaBean");

        Contact contact = new Contact();
        contact.setId(1);
        contact.setName("Cindy");

        String name = jdbi.withHandle(handle -> {
            handle.execute("create table contacts (id int primary key, name varchar(100))");

            handle.createUpdate("insert into contacts (id, name) values (:id, :name)")
                    .bindBean(contact)
                    .execute();

            return handle.createQuery("select name from contacts where id = :id")
                    .bind("id", 1)
                    .mapTo(String.class)
                    .one();
        });

        assert name.equals("Cindy");
    }

    /**
     * You can also bind an Object’s public fields
     */
    public void bindAnObjectPublicFields()
    {
        Contact contact = new Contact();
        contact.setId(1);
        contact.setName("Cindy");

        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_bindAnObjectPublicFields");
        String name = jdbi.withHandle(handle -> {
            handle.execute("create table contacts (id int primary key, name varchar(100))");

            handle.createUpdate("insert into contacts (id, name) values (:id, :name)")
                    .bindFields(contact)
                    .execute();

            return handle.createQuery("select name from contacts where id = :id")
                    .bind("id", 1)
                    .mapTo(String.class)
                    .one();
        });

        assert name.equals("Cindy");
    }

    /**
     * you can bind public, parameterless methods of an Object
     */
    public void bindPublicParameterlessMethods()
    {
        Contact contact = new Contact();
        contact.setId(1);
        contact.setName("Cindy");

        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_bindPublicParameterlessMethods");
        String name = jdbi.withHandle(handle -> {
            handle.execute("create table contacts (id int primary key, name varchar(100))");

            handle.createUpdate("insert into contacts (id, name) values (:getId, :getName)")
                    .bindMethods(contact)
                    .execute();

            return handle.createQuery("select name from contacts where id = :id")
                    .bind("id", 1)
                    .mapTo(String.class)
                    .one();
        });

        assert name.equals("Cindy");
    }

    public static void main(String[] args) {
        ArgumentsDemo argumentsDemo = new ArgumentsDemo();
        argumentsDemo.positionalArguments();
        argumentsDemo.namedArguments();

        argumentsDemo.bindMultipleArgumentsByMap();
        argumentsDemo.bindMultipleValuesatOnce();
        argumentsDemo.bindMultipleArgumentsViaBean();
        argumentsDemo.bindAnObjectPublicFields();
        argumentsDemo.bindPublicParameterlessMethods();
    }
}
