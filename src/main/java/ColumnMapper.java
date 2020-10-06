import model.User;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class ColumnMapper {

    /**
     * ColumnMapper is a functional interface,
     * which maps a column from the current row of a JDBC ResultSet to a mapped type.
     */
    public void columnMapper()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_columnMapper");
        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            List<String> names = handle
                    .select("SELECT name FROM user ORDER BY id ASC")
                    .map((rs, col, ctx) -> rs.getString(col))
                    .list();

            System.out.println(names);
        });
    }

    public static void main(String[] args) {
        ColumnMapper columnMapper = new ColumnMapper();
        columnMapper.columnMapper();
    }
}
