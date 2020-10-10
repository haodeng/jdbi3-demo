package model.mapentry;

import java.beans.ConstructorProperties;

public class User {
    private int id;
    private String name;

    /**
     * The @ConstructorProperties annotations tells Jdbi the property name of each constructor parameter,
     * so it can figure out which column corresponds to each constructor parameter.
     *
     * Enabling the -parameters Java compiler flag removes the need for the @ConstructorProperties annotation
     */
    @ConstructorProperties({"id", "name"})
    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "(User id:" + id +
                ", name:" + name +
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
}
