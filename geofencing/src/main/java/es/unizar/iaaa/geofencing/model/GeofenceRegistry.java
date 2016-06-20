package es.unizar.iaaa.geofencing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Map;

import javax.persistence.*;

@Entity
@Table(name = "GEOFENCES_REGISTRY")
public class GeofenceRegistry {

    private Long id;
    private Map<Long, Date> entering;
    private Map<Long, Date> leaving;
    private Map<Long, Date> inside;
    private User user;
    private Date date;

    public GeofenceRegistry() {
    }

    public GeofenceRegistry(@JsonProperty("id") Long id, @JsonProperty("entering") Map<Long, Date> entering,
                            @JsonProperty("leaving") Map<Long, Date> leaving, @JsonProperty("inside") Map<Long, Date> inside,
                            @JsonProperty("user") User user, @JsonProperty("date") Date date) {
        this.id = id;
        this.entering = entering;
        this.leaving = leaving;
        this.inside = inside;
        this.user = user;
        this.date = date;
    }

    @Id
    @GeneratedValue
    @Column(name = "ID", unique = true, nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "ENTERING_KEY")
    @Column(name = "ENTERING_VALUE")
    @CollectionTable(name = "ENTERING_MAPPING", joinColumns = @JoinColumn(name = "ENTERING_ID", referencedColumnName = "ID"))
    public Map<Long, Date> getEntering() {
        return entering;
    }

    public void setEntering(Map<Long, Date> entering) {
        this.entering = entering;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "LEAVING_KEY")
    @Column(name = "LEAVING_VALUE")
    @CollectionTable(name = "LEAVING_MAPPING", joinColumns = @JoinColumn(name = "LEAVING_ID", referencedColumnName = "ID"))
    public Map<Long, Date> getLeaving() {
        return leaving;
    }

    public void setLeaving(Map<Long, Date> leaving) {
        this.leaving = leaving;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "INSIDE_KEY")
    @Column(name = "INSIDE_VALUE")
    @CollectionTable(name = "INSIDE_MAPPING", joinColumns = @JoinColumn(name = "INSIDE_ID", referencedColumnName = "ID"))
    public Map<Long, Date> getInside() {
        return inside;
    }

    public void setInside(Map<Long, Date> inside) {
        this.inside = inside;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(name = "DATE", nullable = false, length = 30)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
