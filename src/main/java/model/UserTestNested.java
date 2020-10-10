package model;

import org.jdbi.v3.core.mapper.Nested;

import java.beans.ConstructorProperties;

public class UserTestNested {
    private int id;
    private String name;
    private Address address;

    /**
     * The @ConstructorProperties annotations tells Jdbi the property name of each constructor parameter,
     * so it can figure out which column corresponds to each constructor parameter.
     *
     * Enabling the -parameters Java compiler flag removes the need for the @ConstructorProperties annotation
     */
//    @ConstructorProperties({"id", "name"})
    public UserTestNested(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Nested constructor-injected types can be mapped using the @Nested annotation
     */
    @ConstructorProperties({"id", "name", "address"})
    public UserTestNested(int id, String name, @Nested Address address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    @Override
    public String toString()
    {
        return "(User id:" + id +
                ", name:" + name +
                ", address:" + address +
                ")";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
