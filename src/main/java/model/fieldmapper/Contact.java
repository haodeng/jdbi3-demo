package model.fieldmapper;

public class Contact {
    public int id;
    public String name;

    @Override
    public String toString()
    {
        return "(ContactTestFieldMapper id:" + id + ", name:" + name + ")";
    }
}
