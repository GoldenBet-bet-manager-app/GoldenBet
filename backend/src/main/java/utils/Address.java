package utils;


import java.io.Serializable;

/**
 * Represents a physical address.
 * Implements Serializable for network transmission via TCP.
 * @author Γιάννης Μονοχολιάς
 */
public class Address implements Serializable {


    private String street;
    private String streetNumber;
    private String city;
    private String zipCode;

    /**
     * Default constructor for Address.
     */
    public Address(String street, String streetNumber, String city, String zipCode) {
        this.street = street;
        this.streetNumber = streetNumber;
        this.city = city;
        this.zipCode = zipCode;
    }

    public Address ( ) {

    }


    // --- Getters and Setters ---

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
}