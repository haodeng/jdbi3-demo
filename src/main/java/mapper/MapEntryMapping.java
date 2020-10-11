package mapper;

import com.google.common.collect.Multimap;
import model.mapentry.Phone;
import model.mapentry.User;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.guava.GuavaPlugin;

import java.util.Map;

public class MapEntryMapping {

    /**
     * Out of the box, Jdbi registers a RowMapper<Map.Entry<K,V>>.
     * Since each row in the result set is a Map.Entry<K,V>,
     * the entire result set can be easily collected into a Map<K,V> (or Guava’s Multimap<K,V>)
     */
    public void mapEntry()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_mapEntry");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            handle.execute("create table phone (id int primary key, user_id int, name varchar(100), number varchar(100))");
            handle.execute("insert into phone (id, user_id, name, number) values (?, ?, ?, ?)", 1, 1, "home1", "233343434");
            handle.execute("insert into phone (id, user_id, name, number) values (?, ?, ?, ?)", 2, 2, "work1", "897646363");

            //A mapper must be registered for both the key and value type.
            handle.registerRowMapper(ConstructorMapper.factory(User.class, "u"))
                    .registerRowMapper(ConstructorMapper.factory(Phone.class, "p"));

            String sql = "select u.id u_id, u.name u_name, p.id p_id, p.number p_number " +
                    "from user u left join phone p on u.id = p.user_id";
            Map<User, Phone> userPhoneMap = handle.createQuery(sql)
                    .collectInto(new GenericType<Map<User, Phone>>() {});
            System.out.println(userPhoneMap);

            /*
            In the preceding example, the User mapper uses a "u" column name prefix,
            and the Phone mapper uses "p".
            Since each mapper only reads columns with the expected prefix,
            the respective id columns are unambiguous.

            A unique index (e.g. by ID column) can be obtained by setting the key column name
             */
            Map<Integer, User> userMap = handle.createQuery("select * from user")
                    .setMapKeyColumn("id")
                    .registerRowMapper(ConstructorMapper.factory(User.class))
                    .collectInto(new GenericType<Map<Integer, User>>() {});
            System.out.println(userMap);

        });
    }

    /**
     * Set both the key and value column names to gather a two-column query into a map result
     */
    public void mapEntry_keyValueColumn()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_mapEntry_keyValueColumn");
        jdbi.useHandle(handle -> {
            handle.execute("create table config (key varchar(100) primary key, value varchar(100))");
            handle.execute("insert into config (key, value) values (?, ?)", "foo", "bar");
            handle.execute("insert into config (key, value) values (?, ?)", "ping", "pong");

            Map<String, String> map = handle.createQuery("select key, value from config")
                    .setMapKeyColumn("key")
                    .setMapValueColumn("value")
                    .collectInto(new GenericType<Map<String, String>>() {});
            System.out.println(map);
        });
    }

    /**
     * All the above examples assume a one-to-one key/value relationship.
     * What if there is a one-to-many relationship?
     *
     * Google Guava provides a Multimap type, which supports mapping multiple values per key.
     *
     * First, follow the instructions in the Google Guava section to install GuavaPlugin into Jdbi.
     *
     * Then, simply ask for a Multimap instead of a Map
     */
    public void mapEntry_GuavaMultimap()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_mapEntry_GuavaMultimap");
        //install the plugin into your Jdbi instance
        jdbi.installPlugin(new GuavaPlugin());

        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            handle.execute("create table phone (id int primary key, user_id int, name varchar(100), number varchar(100))");
            handle.execute("insert into phone (id, user_id, name, number) values (?, ?, ?, ?)", 1, 1, "home1", "233343434");
            handle.execute("insert into phone (id, user_id, name, number) values (?, ?, ?, ?)", 2, 1, "cell", "233343439");
            handle.execute("insert into phone (id, user_id, name, number) values (?, ?, ?, ?)", 3, 2, "work1", "897646363");

            String sql = "select u.id u_id, u.name u_name, p.id p_id, p.number p_number "
                    + "from user u left join phone p on u.id = p.user_id";
            Multimap<User, Phone> userPhoneMap = handle.createQuery(sql)
                    .registerRowMapper(ConstructorMapper.factory(User.class, "u"))
                    .registerRowMapper(ConstructorMapper.factory(Phone.class, "p"))
                    .collectInto(new GenericType<Multimap<User, Phone>>() {});
            System.out.println(userPhoneMap);
        });
    }

    public static void main(String[] args) {
        MapEntryMapping mapping = new MapEntryMapping();
        mapping.mapEntry();
        mapping.mapEntry_keyValueColumn();

        mapping.mapEntry_GuavaMultimap();
    }
}
