package bean;

import org.jdbi.v3.core.mapper.Nested;

/**
 * For BeanMapper
 */
public class UserBean {
    private int id;
    private String name;
    private AddressBean address;

    @Override
    public String toString()
    {
        return "(UserBean id:" + id +
                ", name:" + name +
                ", address:" + address +
                ")";
    }

    //Nested Java Bean types can be mapped using the @Nested annotation
    //The @Nested annotation can be placed on either the getter or setter method
    @Nested
    public AddressBean getAddress() {
        return address;
    }

    public void setAddress(AddressBean address) {
        this.address = address;
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
}
