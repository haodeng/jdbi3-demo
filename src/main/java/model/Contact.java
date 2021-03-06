package model;

import java.beans.ConstructorProperties;

public class Contact {
    public int id;
    public String name;

    public Contact(){}

    @ConstructorProperties({"id", "name"})
    public Contact(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "(Contact id:" + id + ", name:" + name + ")";
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
