package bean;

public class ContactBean {
    public int id;
    public String name;

    public ContactBean(){}


    @Override
    public String toString()
    {
        return "(ContactBean id:" + id + ", name:" + name + ")";
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
