package mapper;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMappers;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.mapper.RowMapper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * A mapper.RowMapperFactory can produce row mappers for arbitrary types.
 *
 * Implementing a factory might be preferable to a regular row mapper if:
 *
 * The mapper implementation is generic, and could apply to multiple mapped types.
 * For example, Jdbi provides a generalized BeanMapper, which maps columns to bean properties for any bean class.
 *
 * The mapped type has a generic signature, and/or the mapper could be composed from other registered mappers.
 * For example, Jdbi provides a Map.Entry<K,V> mapper, provided a mapper is registered for types K and V.
 *
 * You want to bundle multiple mappers into a single class.
 */
public class RowMapperFactory {
    public final class Pair<L, R> {
        public final L left;
        public final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString()
        {
            return "(Pair, left:" + left + ", right:" + right + ")";
        }
    }

    /**
     * The factory should produce a RowMapper<Pair<L, R>> for any Pair<L, R> type,
     * where the L type is mapped from the first column, and R from the second
     * —- assuming there are column mappers registered for both L and R.
     */
    public class PairMapperFactory implements org.jdbi.v3.core.mapper.RowMapperFactory
    {
        /**
         * The build method accepts a mapped type, and a config registry.
         * It may return Optional.of(someMapper) if it knows how to map that type,
         * or Optional.empty() otherwise.
         */
        public Optional<RowMapper<?>> build(Type type, ConfigRegistry config) {
            if (!Pair.class.equals(GenericTypes.getErasedType(type))) {
                return Optional.empty();
            }

            //extract the L and R generic parameters from the mapped type
            Type leftType = GenericTypes.resolveType(Pair.class.getTypeParameters()[0], type);
            Type rightType = GenericTypes.resolveType(Pair.class.getTypeParameters()[1], type);

            /*
            The config registry is a locator for config classes.
            So when we call config.get(mapper.ColumnMappers.class),
            we get back a mapper.ColumnMappers instance with the current column mapper configuration.
             */
            ColumnMappers columnMappers = config.get(ColumnMappers.class);

            ColumnMapper<?> leftMapper = columnMappers.findFor(leftType)
                    .orElseThrow(() -> new NoSuchMapperException(
                            "No column mapper registered for Pair left parameter " + leftType));
            ColumnMapper<?> rightMapper = columnMappers.findFor(rightType)
                    .orElseThrow(() -> new NoSuchMapperException(
                            "No column mapper registered for Pair right parameter " + rightType));

            RowMapper<?> pairMapper = (rs, ctx) ->
                    new Pair(leftMapper.map(rs, 1, ctx), // In JDBC, column numbers start at 1
                            rightMapper.map(rs, 2, ctx));

            return Optional.of(pairMapper);
        }
    }

    public void rowMapperFactory()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_rowMapperFactory");
        //Row mapper factories may be registered similar to regular row mappers:
        jdbi.registerRowMapper(new PairMapperFactory());

        jdbi.useHandle(handle -> {
            handle.execute("create table config (key varchar(100), value varchar(100))");
            handle.execute("insert into config (key, value) values (?, ?)", "foo", "bar");
            handle.execute("insert into config (key, value) values (?, ?)", "ping", "pong");

            List<Pair<String, String>> configPairs = handle
                    .createQuery("SELECT key, value FROM config")
                    .mapTo(new GenericType<Pair<String, String>>() {})
                    .list();

            System.out.println(configPairs);
        });
    }

    public static void main(String[] args) {
        RowMapperFactory r = new RowMapperFactory();
        r.rowMapperFactory();
    }
}
