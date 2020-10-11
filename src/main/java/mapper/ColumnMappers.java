package mapper;

import model.Money;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ColumnMappers {

    /**
     * ColumnMapper is a functional interface,
     * which maps a column from the current row of a JDBC ResultSet to a mapped type.
     */
    public void columnMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_columnMapper");
        jdbi.useHandle(handle -> {
            handle.execute("create table transactions (amount varchar(100), account_id varchar(100))");
            handle.execute("insert into transactions (amount, account_id) values (?, ?)", "100", "1a");
            handle.execute("insert into transactions (amount, account_id) values (?, ?)", "200", "1b");

            List<Money> amounts = handle
                    .select("select amount from transactions")
                    .map((rs, col, ctx) -> Money.parse(rs.getString(col)))
                    .list();

            System.out.println(amounts);
        });
    }

    //Column mappers may be defined as classes, which allows for re-use
    public class MoneyMapper implements ColumnMapper<Money> {
        public Money map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
            return Money.parse(r.getString(columnNumber));
        }
    }

    public void columnMapperAsClass()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_columnMapperAsClass");
        jdbi.useHandle(handle -> {
            handle.execute("create table transactions (amount varchar(100), account_id varchar(100))");
            handle.execute("insert into transactions (amount, account_id) values (?, ?)", "100", "1a");
            handle.execute("insert into transactions (amount, account_id) values (?, ?)", "200", "1b");

            List<Money> amounts = handle
                    .select("select amount from transactions")
                    .map(new MoneyMapper())
                    .list();

            System.out.println(amounts);
        });
    }

    /**
     * Column mappers may be registered for specific types.
     * This simplifies usage, requiring only that you specify what type you want to map to.
     * Jdbi automatically looks up the mapper from the registry, and uses it.
     */
    public void columnMappersRegistry()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_columnMappersRegistry");
        jdbi.registerColumnMapper(Money.class,
                (rs, col, ctx) -> Money.parse(rs.getString(col)));

        jdbi.useHandle(handle -> {
            handle.execute("create table transactions (amount varchar(100), account_id varchar(100))");
            handle.execute("insert into transactions (amount, account_id) values (?, ?)", "100", "1a");
            handle.execute("insert into transactions (amount, account_id) values (?, ?)", "200", "1b");

            List<Money> amounts = handle
                    .select("select amount from transactions")
                    .mapTo(Money.class)
                    .list();

            System.out.println(amounts);
        });
    }

    /**
     * A mapper which implements ColumnMapper with an explicit mapped type
     * (such as the MoneyMapper class in the previous section) may be registered
     * without specifying the mapped type
     *
     * When this method is used,
     * Jdbi inspects the generic class signature of the mapper to automatically discover the mapped type.
     */
    public void columnMappersRegistryWithMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_columnMappersRegistry");
        jdbi.registerColumnMapper(new MoneyMapper());

        jdbi.useHandle(handle -> {
            handle.execute("create table transactions (amount varchar(100), account_id varchar(100))");
            handle.execute("insert into transactions (amount, account_id) values (?, ?)", "100", "1a");
            handle.execute("insert into transactions (amount, account_id) values (?, ?)", "200", "1b");

            List<Money> amounts = handle
                    .select("select amount from transactions")
                    .mapTo(Money.class)
                    .list();

            System.out.println(amounts);
        });
    }

    public static void main(String[] args) {
        ColumnMappers mappers = new ColumnMappers();
        mappers.columnMapper();
        mappers.columnMapperAsClass();
        mappers.columnMappersRegistry();
        mappers.columnMappersRegistryWithMapper();
    }
}
