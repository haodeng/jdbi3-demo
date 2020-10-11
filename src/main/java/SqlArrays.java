import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Arrays;
import java.util.List;

public class SqlArrays {
    //Jdbi can bind/map Java arrays to/from SQL arrays
    public void bindArrays()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_bindArrays");
        //Any Java array element type you want binding support for needs to be registered
        // with Jdbi’s SqlArrayTypes registry.
        // An array type that is directly supported by your JDBC driver can be registered using
        jdbi.registerArrayType(int.class, "integer");

        jdbi.useHandle(handle -> {
            handle.execute("create table groups (id int primary key, user_ids array[20])");
            handle.createUpdate("insert into groups (id, user_ids) values (:id, :userIds)")
                    .bind("id", 1)
                    .bind("userIds", new int[] { 10, 5, 70 })
                    .execute();

            int[] userIds = handle.createQuery("select user_ids from groups where id = :id")
                    .bind("id", 1)
                    .mapTo(int[].class)
                    .one();
            for (int i = 0; i < userIds.length; i++)
                System.out.print(i + ",");

            System.out.println();
        });
    }

    public void bindArrays_useCollections()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_bindArrays");
        //Use Integer
        jdbi.registerArrayType(Integer.class, "integer");

        /*
        You can also use Collections in place of arrays,
        but you’ll need to provide the element type if you’re using the fluent API, since it’s erased
         */
        jdbi.useHandle(handle -> {
            handle.execute("create table groups (id int primary key, user_ids array[20])");
            handle.createUpdate("insert into groups (id, user_ids) values (:id, :userIds)")
                    .bind("id", 2)
                    .bindArray("userIds", Integer.class, Arrays.asList(10, 5, 70))
                    .execute();

            List<Integer> userIdList = handle.createQuery("select user_ids from groups where id = :id")
                    .bind("id", 2)
                    .mapTo(new GenericType<List<Integer>>() {})
                    .one();
            System.out.println(userIdList);
        });
    }

    //Use @SingleValue for mapping an array result with the SqlObject API
    public interface GroupsDao {
        @SqlQuery("select user_ids from groups where id = ?")
        @SingleValue
        List<Integer> getUserIds(int groupId);
    }

    public void bindArrays_bySqlObject()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_bindArrays_bySqlObject");
        jdbi.registerArrayType(int.class, "integer");
        jdbi.installPlugin(new SqlObjectPlugin());

        jdbi.useHandle(handle -> {
            handle.execute("create table groups (id int primary key, user_ids array[20])");
            handle.createUpdate("insert into groups (id, user_ids) values (:id, :userIds)")
                    .bind("id", 1)
                    .bind("userIds", new int[] { 10, 5, 70 })
                    .execute();

            GroupsDao groupsDao = handle.attach(GroupsDao.class);
            List<Integer> userIdList = groupsDao.getUserIds(1);
            System.out.println(userIdList);
        });
    }

    public static void main(String[] args) {
        SqlArrays sqlArrays = new SqlArrays();
        sqlArrays.bindArrays();
        sqlArrays.bindArrays_useCollections();

        sqlArrays.bindArrays_bySqlObject();
    }
}
