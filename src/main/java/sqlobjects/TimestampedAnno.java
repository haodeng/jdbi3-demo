package sqlobjects;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Map;

public class TimestampedAnno {

    /**
     * You can annotate any statement with @Timestamped to bind an OffsetDateTime,
     * of which the value is the current time, under the binding now
     */
    public interface TestDao {
        @SqlUpdate("insert into times(val) values(:now)")
        @Timestamped
        int insert();
    }

    public void test() {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:GetGeneratedKeysAnno_test");
        jdbi.installPlugin(new SqlObjectPlugin());

        try (Handle handle = jdbi.open()) {
            handle.execute("create table times (val timestamp)");

            TestDao testDao = handle.attach(TestDao.class);
            testDao.insert();

            List<Map<String, Object>> timesMap = handle.select("select * from times").mapToMap().list();
            timesMap.stream().forEach(entry -> System.out.println(entry));
        }
    }

    public static void main(String[] args) {
        TimestampedAnno test = new TimestampedAnno();
        test.test();
    }
}
