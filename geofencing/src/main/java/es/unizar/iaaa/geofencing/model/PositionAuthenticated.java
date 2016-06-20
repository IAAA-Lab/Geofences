package es.unizar.iaaa.geofencing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PositionAuthenticated {

    private String authorization;
    private Position position;

    public PositionAuthenticated(@JsonProperty("authorization") String authorization,
                                 @JsonProperty("position") Position position) {
        this.authorization = authorization;
        this.position = position;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
