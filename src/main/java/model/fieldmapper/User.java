package model.fieldmapper;

import model.Address;
import org.jdbi.v3.core.mapper.Nested;

/**
 * For testing FieldMapper
 */
public class UserTestFieldMapper {
    //FieldMapper uses reflection to map database columns directly to object fields (including private fields).
    public int id;
    public String name;

    //Nested field-mapped types can be mapped using the @Nested annotation
    @Nested
    public AddressTestFieldMapper address;

    @Override
    public String toString()
    {
        return "(User id:" + id +
                ", name:" + name +
                ", address:" + address +
                ")";
    }
}
