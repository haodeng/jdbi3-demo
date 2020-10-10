package model.fieldmapper;

public class ContactTestFieldMapper {
    public int id;
    public String name;

    @Override
    public String toString()
    {
        return "(ContactTestFieldMapper id:" + id + ", name:" + name + ")";
    }
}
