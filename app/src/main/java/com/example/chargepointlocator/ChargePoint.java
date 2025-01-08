package com.example.chargepointlocator;

// Class to represent a Charge Point with details such as location, connector information, and status
public class ChargePoint {
    // Latitude of the charge point location
    private String latitude;
    // Longitude of the charge point location
    private String longitude;
    // Unique identifier for the connector
    private String connectorID;
    // Type of connector (e.g., Type 2, CHAdeMO)
    private String connectorType;
    // Unique reference ID for the charge point device
    private String referenceID;
    // Town where the charge point is located
    private String town;
    // County or administrative area of the charge point
    private String county;
    // Postcode for the charge point location
    private String postcode;
    // Current status of the charge device (e.g., Available, In Use, Out of Service)
    private String chargeDeviceStatus;

    /**
     * Constructor to initialize a ChargePoint object with its attributes.
     *
     * @param latitude          Latitude of the charge point
     * @param longitude         Longitude of the charge point
     * @param connectorID       Unique ID of the connector
     * @param connectorType     Type of connector (e.g., Type 2, CHAdeMO)
     * @param referenceID       Reference ID for the charge point
     * @param town              Town where the charge point is located
     * @param county            County or region of the charge point
     * @param postcode          Postcode of the charge point location
     * @param chargeDeviceStatus Current status of the charge point
     */
    public ChargePoint(String latitude, String longitude, String connectorID, String connectorType, String referenceID, String town, String county, String postcode, String chargeDeviceStatus) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.connectorID = connectorID;
        this.connectorType = connectorType;
        this.referenceID = referenceID;
        this.town = town;
        this.county = county;
        this.postcode = postcode;
        this.chargeDeviceStatus = chargeDeviceStatus;
    }

    // Getter for latitude
    public String getLatitude() {
        return latitude;
    }

    // Getter for longitude
    public String getLongitude() {
        return longitude;
    }

    // Getter for connector ID
    public String getConnectorID(){
        return connectorID;
    }

    // Getter for connector type
    public String getConnectorType(){
        return connectorType;
    }

    // Getter for reference ID
    public String getReferenceID() {
        return referenceID;
    }

    // Getter for town
    public String getTown() {
        return town;
    }

    // Getter for county
    public String getCounty() {
        return county;
    }

    // Getter for postcode
    public String getPostcode() {
        return postcode;
    }

    // Getter for the charge device status
    public String getChargeDeviceStatus() {
        return chargeDeviceStatus;
    }
}
