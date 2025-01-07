package com.example.chargepointlocator;

public class ChargePoint {
    private String latitude;
    private String longitude;
    private String connectorID;
    private String connectorType;
    private String referenceID;
    private String town;
    private String county;
    private String postcode;
    private String chargeDeviceStatus;


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

    public String getLatitude() {
        return latitude;
    }
    public String getLongitude() {
        return longitude;
    }
    public String getConnectorID(){
        return connectorID;
    }

    public String getConnectorType(){
        return connectorType;
    }

    public String getReferenceID() {
        return referenceID;
    }

    public String getTown() {
        return town;
    }

    public String getCounty() {
        return county;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getChargeDeviceStatus() {
        return chargeDeviceStatus;
    }
}
