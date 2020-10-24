package sqlobjects;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * Sometimes when using advanced SQL features like Arrays,
 * a container type like int[] or List<Integer> can ambiguously mean either "a single SQL int[]" or "a ResultSet of int".
 *
 * Since arrays are not commonly used in normalized schema,
 * SQL Object assumes by default that you are collecting a ResultSet into a container object.
 * You can annotate a return type as @SingleValue to override this.
 */
public class SingleValueAnno {
    public interface UserDao {
        @SqlUpdate("insert into users (id, name, roles) values (?, ?, ?)")
        int insert(long id, String name, String roleIds);

        //Array role_id
        @SqlUpdate("create table users (id int primary key, name varchar(100), roles ARRAY)")
        void createTable();

        //select a varchar[] column from a single row
        @SqlQuery("select roles from users where id = ?")
        @SingleValue
        List<String> getUserRoles(long userId);
    }

    public void test()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_test");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        UserDao userDao = handle.attach(UserDao.class);

        userDao.createTable();
        //Insert array value (guest, admin)
        userDao.insert(1L, "Bob", "(guest, admin)");
        userDao.insert(2L, "Kim", "(admin)");

        //Expect: [(guest, admin)]
        System.out.println(userDao.getUserRoles(1));
    }

    public static void main(String[] args) {
        SingleValueAnno singleValueAnno = new SingleValueAnno();
        singleValueAnno.test();
    }
}
