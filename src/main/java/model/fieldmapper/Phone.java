package model.fieldmapper;

public class PhoneTestFieldMapper {
    public int id;
    public String name;
    public String number;

    @Override
    public String toString()
    {
        return "(PhoneTestFieldMapper id:" + id + ", name:" + name + ", number:" + number + ")";
    }
}
