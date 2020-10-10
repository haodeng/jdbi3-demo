import model.Contact;
import model.Phone;
import model.User;
import model.UserTestNested;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.JoinRow;
import org.jdbi.v3.core.mapper.JoinRowMapper;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Jdbi provides a simple constructor mapper
 * which uses reflection to assign columns to constructor parameters by name.
 */
public class ConstructorMappers {

    public void constructorMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_constructorMapper");
        jdbi.useHandle(handle -> {
            //The constructor parameter names "id", "name" match the database column names
            handle.registerRowMapper(org.jdbi.v3.core.mapper.reflect.ConstructorMapper.factory(User.class));

            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            Set<User> userSet = handle.createQuery("SELECT * FROM user ORDER BY id ASC")
                    .mapTo(User.class)
                    .collect(Collectors.toSet());

            System.out.println(userSet);
        });
    }

    /**
     * Constructor mappers can be configured with a column name prefix for each mapped constructor parameter.
     * This can help to disambiguate mapping joins,
     * e.g. when two mapped classes have identical property names (like id or name)
     */
    public void constructorMapper_JoinRowMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_constructorMapperJoinTables");
        jdbi.useHandle(handle -> {
            handle.registerRowMapper(ConstructorMapper.factory(Contact.class, "c"));
            handle.registerRowMapper(ConstructorMapper.factory(Phone.class, "p"));
            handle.registerRowMapper(JoinRowMapper.forTypes(Contact.class, Phone.class));

            handle.execute("create table contacts (id int primary key, name varchar(100))");
            handle.execute("insert into contacts (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into contacts (id, name) values (?, ?)", 2, "Bob");

            handle.execute("create table phones (id int primary key, contact_id int, name varchar(100), number varchar(100))");
            handle.execute("insert into phones (id, contact_id, name, number) values (?, ?, ?, ?)", 1, 1, "home1", "233343434");
            handle.execute("insert into phones (id, contact_id, name, number) values (?, ?, ?, ?)", 2, 2, "work1", "897646363");

            List<JoinRow> contactPhones = handle.select("select " +
                    "c.id cid, c.name cname, " +
                    "p.id pid, p.name pname, p.number pnumber " +
                    "from contacts c left join phones p " +
                    "on c.id = p.contact_id")
                    .mapTo(JoinRow.class)
                    .list();

            contactPhones.forEach(contactPhone ->
                    System.out.println(contactPhone.get(Contact.class) + " and " + contactPhone.get(Phone.class)));
        });
    }

    public void constructorMapper_nested()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_constructorMapper_nested");
        jdbi.useHandle(handle -> {
            handle.registerRowMapper(ConstructorMapper.factory(UserTestNested.class));

            handle.execute("create table user (id int primary key, name varchar(100), street varchar(100), city varchar(100), state varchar(100), zip varchar(100))");
            handle.execute("insert into user (id, name, street, city, state, zip) values (?, ?, ?, ?, ?, ?)", 1, "Alice", "am st", "cph", "dk", "2100");
            handle.execute("insert into user (id, name, street, city, state, zip) values (?, ?, ?, ?, ?, ?)", 2, "Bob", "prod st", "cph", "dk", "2200");

            List<UserTestNested> users = handle
                    .select("select id, name, street, city, state, zip from user")
                    .mapTo(UserTestNested.class)
                    .list();

            System.out.println(users);
        });
    }

    public static void main(String[] args) {
        ConstructorMappers mapper = new ConstructorMappers();
        mapper.constructorMapper();

        mapper.constructorMapper_JoinRowMapper();
        mapper.constructorMapper_nested();
    }
}
