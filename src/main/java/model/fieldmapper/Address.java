package model.fieldmapper;

public class Address {
    private String street;
    private String city;
    private String state;
    private String zip;

    @Override
    public String toString()
    {
        return "(AddressTestFieldMapper street:" + street +
                ", city:" + city +
                ", state:" + state +
                ", zip:" + zip +
                ")";
    }
}
