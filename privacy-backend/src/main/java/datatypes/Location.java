package datatypes;

public class Location {

    private double lat;
    private double lon;
    private double risk;
    private String color;

    public Location(double lat, double lon, double risk, String color) {
        this.lat = lat;
        this.lon = lon;
        this.risk = risk;
        this.color = color;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getRisk() {
        return risk;
    }

    public void setRisk(double risk) {
        this.risk = risk;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
