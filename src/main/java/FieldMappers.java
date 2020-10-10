import model.fieldmapper.ContactTestFieldMapper;
import model.fieldmapper.PhoneTestFieldMapper;
import model.fieldmapper.UserTestFieldMapper;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.JoinRow;
import org.jdbi.v3.core.mapper.JoinRowMapper;
import org.jdbi.v3.core.mapper.reflect.FieldMapper;

import java.util.List;

/**
 * FieldMapper uses reflection to map database columns directly to object fields
 * (including private fields).
 */
public class FieldMappers {
    public class MyUser {
        public int id;
        public String name;
    }

    public void fieldMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_fieldMapper");
        jdbi.useHandle(handle -> {
            handle.registerRowMapper(FieldMapper.factory(UserTestFieldMapper.class));

            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            List<UserTestFieldMapper> users = handle
                    .createQuery("select id, name from user")
                    .mapTo(UserTestFieldMapper.class)
                    .list();

            System.out.println(users);
        });
    }

    public void fieldMapper_configColumnNamePrefix()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_fieldMapper_configColumnNamePrefix");
        jdbi.useHandle(handle -> {
            handle.registerRowMapper(FieldMapper.factory(ContactTestFieldMapper.class, "c"));
            handle.registerRowMapper(FieldMapper.factory(PhoneTestFieldMapper.class, "p"));
            handle.registerRowMapper(JoinRowMapper.forTypes(ContactTestFieldMapper.class, PhoneTestFieldMapper.class));

            handle.execute("create table contacts (id int primary key, name varchar(100))");
            handle.execute("insert into contacts (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into contacts (id, name) values (?, ?)", 2, "Bob");

            handle.execute("create table phones (id int primary key, contact_id int, name varchar(100), number varchar(100))");
            handle.execute("insert into phones (id, contact_id, name, number) values (?, ?, ?, ?)", 1, 1, "home1", "233343434");
            handle.execute("insert into phones (id, contact_id, name, number) values (?, ?, ?, ?)", 2, 2, "work1", "897646363");


            List<JoinRow> contactPhones = handle.select("select "
                    + "c.id cid, c.name cname, "
                    + "p.id pid, p.name pname, p.number pnumber "
                    + "from contacts c left join phones p on c.id = p.contact_id")
                    .mapTo(JoinRow.class)
                    .list();

            contactPhones.forEach(contactPhone ->
                    System.out.println(contactPhone.get(ContactTestFieldMapper.class) +
                            " and " + contactPhone.get(PhoneTestFieldMapper.class)));
        });
    }

    public void fieldMapper_nested()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_beanMapper_nested");
        jdbi.useHandle(handle -> {
            handle.registerRowMapper(FieldMapper.factory(UserTestFieldMapper.class));

            handle.execute("create table users (id int primary key, name varchar(100), street varchar(100), city varchar(100), state varchar(100), zip varchar(100))");
            handle.execute("insert into users (id, name, street, city, state, zip) values (?, ?, ?, ?, ?, ?)", 1, "Alice", "am st", "cph", "dk", "2100");
            handle.execute("insert into users (id, name, street, city, state, zip) values (?, ?, ?, ?, ?, ?)", 2, "Bob", "prod st", "cph", "dk", "2200");

            List<UserTestFieldMapper> users = handle
                    .select("select id, name, street, city, state, zip from users")
                    .mapTo(UserTestFieldMapper.class)
                    .list();
            System.out.println(users);
        });
    }

    public static void main(String[] args) {
        FieldMappers mappers = new FieldMappers();
        mappers.fieldMapper();
        mappers.fieldMapper_configColumnNamePrefix();
        mappers.fieldMapper_nested();
    }
}
