package mapper;

import bean.ContactBean;
import bean.PhoneBean;
import bean.UserBean;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.JoinRow;
import org.jdbi.v3.core.mapper.JoinRowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;

import java.util.List;

public class BeanMappers {

    public void beanMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_beanMapper");
        jdbi.useHandle(handle -> {
            //Register a bean mapper for your mapped class, using the factory() method
            handle.registerRowMapper(BeanMapper.factory(UserBean.class));

            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            List<UserBean> users = handle
                    .createQuery("select id, name from user")
                    .mapTo(UserBean.class)
                    .list();
            System.out.println(users);

            //Alternatively, call mapToBean() instead of registering a bean mapper
            users = handle
                    .createQuery("select id, name from user")
                    .mapToBean(UserBean.class)
                    .list();

            System.out.println(users);
        });
    }

    public void beanMapper_configColumnNamePrefix()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_beanMapper_configColumnNamePrefix");
        jdbi.useHandle(handle -> {
            /*
            Bean mappers can be configured with a column name prefix for each mapped property.
            This can help to disambiguate mapping joins,
            e.g. when two mapped classes have identical property names (like id or name):
             */
            handle.registerRowMapper(BeanMapper.factory(ContactBean.class, "c"));
            handle.registerRowMapper(BeanMapper.factory(PhoneBean.class, "p"));
            handle.registerRowMapper(JoinRowMapper.forTypes(ContactBean.class, PhoneBean.class));

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
                    System.out.println(contactPhone.get(ContactBean.class) + " and " + contactPhone.get(PhoneBean.class)));
        });
    }

    public void beanMapper_nested()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_beanMapper_nested");
        jdbi.useHandle(handle -> {
            //Register a bean mapper for your mapped class, using the factory() method
            handle.registerRowMapper(BeanMapper.factory(UserBean.class));

            handle.execute("create table users (id int primary key, name varchar(100), street varchar(100), city varchar(100), state varchar(100), zip varchar(100))");
            handle.execute("insert into users (id, name, street, city, state, zip) values (?, ?, ?, ?, ?, ?)", 1, "Alice", "am st", "cph", "dk", "2100");
            handle.execute("insert into users (id, name, street, city, state, zip) values (?, ?, ?, ?, ?, ?)", 2, "Bob", "prod st", "cph", "dk", "2200");

            List<UserBean> users = handle
                    .select("select id, name, street, city, state, zip from users")
                    .mapTo(UserBean.class)
                    .list();
            System.out.println(users);
        });
    }

    public static void main(String[] args) {
        BeanMappers mappers = new BeanMappers();
        mappers.beanMapper();
        mappers.beanMapper_configColumnNamePrefix();

        mappers.beanMapper_nested();
    }
}
