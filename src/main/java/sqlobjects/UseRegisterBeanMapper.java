package sqlobjects;

import bean.UserBean;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public class UseRegisterBeanMapper {
    public interface UserDao {
        @SqlUpdate("insert into users (id, name) values (?, ?)")
        int insert(long id, String name);

        @SqlUpdate("create table users (id int primary key, name varchar(100))")
        void createTable();

        @SqlQuery("select * from users")
        @RegisterBeanMapper(UserBean.class)
        List<UserBean> list();
    }

    public void testBeanMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_testBeanMapper");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        UserDao userDao = handle.attach(UserDao.class);

        userDao.createTable();
        userDao.insert(1L, "Bob");
        userDao.insert(2L, "Kim");

        userDao.list().forEach(user -> System.out.println(user));
    }

    public static void main(String[] args) {
        UseRegisterBeanMapper mapper = new UseRegisterBeanMapper();
        mapper.testBeanMapper();
    }
}
