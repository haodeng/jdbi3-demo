package model;

import java.beans.ConstructorProperties;

public class Address {
    private String street;
    private String city;
    private String state;
    private String zip;

    /**
     * The @ConstructorProperties annotations tells Jdbi the property name of each constructor parameter,
     * so it can figure out which column corresponds to each constructor parameter.
     *
     * Enabling the -parameters Java compiler flag removes the need for the @ConstructorProperties annotation
     */
    @ConstructorProperties({"street", "city", "state", "zip"})
    public Address(String street, String city, String state, String zip) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    @Override
    public String toString()
    {
        return "(Address street:" + street +
                ", city:" + city +
                ", state:" + state +
                ", zip:" + zip +
                ")";
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
