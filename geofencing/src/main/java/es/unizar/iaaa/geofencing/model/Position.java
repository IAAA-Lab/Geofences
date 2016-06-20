package es.unizar.iaaa.geofencing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Geometry;

public class Position {

    private Geometry coordinates;

    public Position() {
    }

    public Position(@JsonProperty("coordinates") Geometry coordinates) {
        this.coordinates = coordinates;
    }

    public Geometry getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Geometry coordinates) {
        this.coordinates = coordinates;
    }
}
