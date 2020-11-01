package sqlobjects;

import model.Contact;
import model.Phone;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DefaultMethods {

    //Must extend SqlObject to use getHandle()
    public interface ContactPhoneDao extends SqlObject {
        @SqlUpdate("insert into contacts (id, name) values (:id, :name)")
        int insertContact(@BindBean Contact contact);

        @SqlBatch("insert into phones (contact_id, name, number) values (:id, :name, :number)")
        void insertPhone(long contactId, @BindBean Iterable<Phone> phones);

        /**
         * Jdbi provides a SqlObject mixin interface with a getHandle method.
         * Make your SQL Object interface extend the SqlObject mixin,
         * then provide your own implementation in a default method
         */
        default void createTables() {
            Handle handle = getHandle();
            handle.execute("create table contacts (id int primary key, name varchar(100))");
            handle.execute("create table phones (contact_id int, name varchar(100), number varchar(100))");
        }
        /**
         * Default methods can also be used to group multiple SQL operations into a single method call
         */
        default long insertFullContact(Contact contact, List<Phone> phones) {
            int id = insertContact(contact);
            insertPhone(id, phones);
            return id;
        }
    }

    public void test()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test");
        jdbi.installPlugin(new SqlObjectPlugin());

        try (Handle handle = jdbi.open()) {
            ContactPhoneDao contactPhoneDao = handle.attach(ContactPhoneDao.class);
            contactPhoneDao.createTables();

            contactPhoneDao.insertContact(new Contact(1, "Bob"));
            Phone phone = new Phone(1, "home", "1029");
            Phone phone2 = new Phone(1, "work", "1028");
            contactPhoneDao.insertPhone(1, Arrays.asList(phone, phone2));

            List<Map<String, Object>> phoneMap =
                    handle.select("select * from phones").mapToMap().list();
            phoneMap.stream().forEach(entry -> System.out.println(entry));

            Phone phone3 = new Phone(2, "home", "10291");
            Phone phone4 = new Phone(2, "work", "10281");
            contactPhoneDao.insertFullContact(
                    new Contact(2, "Peter"), Arrays.asList(phone3, phone4));

            phoneMap = handle.select("select * from phones where contact_id = 2").mapToMap().list();
            phoneMap.stream().forEach(entry -> System.out.println(entry));
        }
    }

    public static void main(String[] args) {
        DefaultMethods defaultMethods = new DefaultMethods();
        defaultMethods.test();
    }
}
