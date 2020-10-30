package sqlobjects;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.statement.SqlScript;

import java.util.Arrays;

public class SqlScriptAnno {

    /**
     * Use @SqlScript to execute one or more statements in a batch.
     * You can define attributes for the template engine to use.
     */
    public interface TestDao {
        @SqlScript("CREATE TABLE cool_table (pk int primary key)")
        @SqlScript("INSERT INTO cool_table VALUES (5), (6), (7)")
        @SqlScript("DELETE FROM cool_table WHERE pk > 5")
        int[] doSomeUpdates();
    }

    public void test()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:SqlScriptAnno_test");
        jdbi.installPlugin(new SqlObjectPlugin());

        Handle handle = jdbi.open();
        TestDao testDao = handle.attach(TestDao.class);

        int[] results = testDao.doSomeUpdates();
        Arrays.stream(results).forEach(System.out::println);
    }

    public static void main(String[] args) {
        SqlScriptAnno test = new SqlScriptAnno();
        test.test();
    }
}
