package sqlobjects;

import model.Money;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UseRegisterColumnMapper {

    public interface AccountDao {
        @SqlUpdate("create table accounts (id long primary key, balance varchar(100))")
        void createTable();

        @SqlUpdate("insert into accounts (id, balance) values (?, ?)")
        int insert(long id, String balance);

        @SqlQuery("select balance from accounts where id = ?")
        @RegisterColumnMapper(MoneyMapper.class)
        Money getBalance(long id);

        class MoneyMapper implements ColumnMapper<Money> {
            public Money map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
                return Money.parse(r.getString(columnNumber));
            }
        }
    }

    public void testColumnMapper() {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_testColumnMapper");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        AccountDao accountDao = handle.attach(AccountDao.class);

        accountDao.createTable();
        accountDao.insert(1L, "1000");
        accountDao.insert(2L, "30000");

        System.out.println(accountDao.getBalance(1));
        System.out.println(accountDao.getBalance(2));
    }

    public static void main(String[] args) {
        UseRegisterColumnMapper mapper = new UseRegisterColumnMapper();
        mapper.testColumnMapper();
    }
}
