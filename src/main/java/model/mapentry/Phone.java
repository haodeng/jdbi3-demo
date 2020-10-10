package model.mapentry;

import java.beans.ConstructorProperties;

public class Phone {
    public int id;
    public String name;
    public String number;

    @ConstructorProperties({"id", "number"})
    public Phone(int id, String number) {
        this.id = id;
        this.number = number;
    }

    @Override
    public String toString()
    {
        return "(Phone id:" + id + ", name:" + name + ", number:" + number + ")";
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
