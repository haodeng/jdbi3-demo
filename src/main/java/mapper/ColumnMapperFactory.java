package mapper;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMappers;
import org.jdbi.v3.core.mapper.NoSuchMapperException;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class ColumnMapperFactory {

    /**
     * A mapper.ColumnMapperFactory can produce column mappers for arbitrary types.
     *
     * Implementing a factory might be preferable to a regular column mapper if:
     *
     * The mapper class is generic, and could apply to multiple mapped types.
     *
     * The type being mapped is generic, and/or the mapper could be composed from other registered mappers.
     *
     * You want to bundle multiple mappers into a single class.
     */
    public class OptionalColumnMapperFactory implements org.jdbi.v3.core.mapper.ColumnMapperFactory {

        /*
        The build method accepts a mapped type, and a config registry.
        It may return Optional.of(someMapper) if it knows how to map that type,
        or Optional.empty() otherwise.
         */
        public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
            if (!Optional.class.equals(GenericTypes.getErasedType(type))) {
                return Optional.empty();
            }

            Type t = GenericTypes.resolveType(Optional.class.getTypeParameters()[0], type);

            ColumnMapper<?> tMapper = config.get(ColumnMappers.class)
                    .findFor(t)
                    .orElseThrow(() -> new NoSuchMapperException(
                            "No column mapper registered for parameter " + t + " of type " + type));

            ColumnMapper<?> optionalMapper = (rs, col, ctx) ->
                    Optional.ofNullable(tMapper.map(rs, col, ctx));

            return Optional.of(optionalMapper);
        }
    }

    public void columnMapperFactory()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_columnMapperFactory");
        //Column mapper factories may be registered similar to regular column mappers
        jdbi.registerColumnMapper(new OptionalColumnMapperFactory());

        jdbi.useHandle(handle -> {
            handle.execute("create table user (id int primary key, name varchar(100))");
            handle.execute("insert into user (id, name) values (?, ?)", 1, "Alice");
            handle.execute("insert into user (id, name) values (?, ?)", 2, "Bob");

            List<Optional<String>> names = handle
                    .createQuery("select name from user")
                    .mapTo(new GenericType<Optional<String>>() {})
                    .list();

            System.out.println(names);
        });
    }

    public static void main(String[] args) {
        ColumnMapperFactory factory = new ColumnMapperFactory();
        factory.columnMapperFactory();
    }
}
