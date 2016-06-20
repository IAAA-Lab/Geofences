package es.unizar.iaaa.geofencing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import es.unizar.iaaa.geofencing.view.View;

@Entity
@Table(name = "RULES")
public class Rule {

    private Long id;
    private Boolean enabled;
    private RuleType type;
    private Integer time;
    private String message;
    private Set<Day> days;
    private Set<Notification> notifications;
    private Geofence geofence;

    public Rule() {
    }

    public Rule(@JsonProperty("id") Long id, @JsonProperty("enabled") Boolean enabled,
                @JsonProperty("type") RuleType type, @JsonProperty("time") Integer time,
                @JsonProperty("message") String message, @JsonProperty("days") Set<Day> days,
                @JsonProperty("notifications") Set<Notification> notifications,
                @JsonProperty("geofence") Geofence geofence) {
        this.id = id;
        this.enabled = enabled;
        this.type = type;
        this.time = time;
        this.message = message;
        this.days = days;
        this.notifications = notifications;
        this.geofence = geofence;
    }

    @Id
    @GeneratedValue
    @Column(name = "ID", unique = true, nullable = false)
    @JsonView({View.NotificationBaseView.class, View.RuleBaseView.class})
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "ENABLED", nullable = false, length = 5)
    @JsonView(View.RuleBaseView.class)
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Column(name = "TYPE", nullable = false, length = 10)
    @JsonView(View.RuleBaseView.class)
    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }

    @Column(name = "TIME", nullable = false, length = 5)
    @JsonView(View.RuleBaseView.class)
    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Column(name = "MESSAGE", nullable = false, length = 130)
    @JsonView({View.NotificationCompleteView.class, View.RuleCompleteView.class})
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "rule", cascade = CascadeType.ALL)
    @JsonView(View.RuleCompleteView.class)
    public Set<Day> getDays() {
        return days;
    }

    public void setDays(Set<Day> days) {
        this.days = days;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "rule", cascade = CascadeType.ALL)
    @JsonView(View.RuleCompleteView.class)
    public Set<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonView(View.RuleCompleteView.class)
    public Geofence getGeofence() {
        return geofence;
    }

    public void setGeofence(Geofence geofence) {
        this.geofence = geofence;
    }

    public String toString() {
        return "Rule(id: " + id + " enabled: " + enabled + " type: " + type.toString() + " time: " + time + " days: " + days.toString()
                + " notifications: " + notifications.toString() + " geofence id: " + geofence.getId() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return id == rule.id &&
                Objects.equals(enabled, rule.enabled) &&
                Objects.equals(type, rule.type) &&
                Objects.equals(time, rule.time) &&
                Objects.equals(days, rule.days) &&
                Objects.equals(notifications, rule.notifications) &&
                Objects.equals(geofence, rule.geofence);
    }
}