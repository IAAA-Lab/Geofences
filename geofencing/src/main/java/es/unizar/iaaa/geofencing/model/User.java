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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import es.unizar.iaaa.geofencing.view.View;

@Entity
@Table(name = "USERS")
public class User {

    private Long id;
    private String nick;
    private String password;
    private String first_name;
    private String last_name;
    private java.sql.Date birthday;
    private String imei;
    private Set<Geofence> geofences;
    private Boolean enabled;
    private String role;
    private java.util.Date last_password_reset_date;
    private Set<Notification> notifications;
    private Set<GeofenceRegistry> geofences_registry;

    public User() {
    }

    public User(@JsonProperty("id") Long id, @JsonProperty("nick") String nick,
                @JsonProperty("password") String password, @JsonProperty("first_name") String first_name,
                @JsonProperty("last_name") String last_name, @JsonProperty("birthday") java.sql.Date birthday,
                @JsonProperty("imei") String imei, @JsonProperty("geofences") Set<Geofence> geofences,
                @JsonProperty("enabled") Boolean enabled, @JsonProperty("role") String role,
                @JsonProperty("last_password_reset_date") java.util.Date last_password_reset_date,
                @JsonProperty("notifications") Set<Notification> notifications,
                @JsonProperty("geofences_registry") Set<GeofenceRegistry> geofences_registry) {
        this.id = id;
        this.nick = nick;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.birthday = birthday;
        this.imei = imei;
        this.geofences = geofences;
        this.enabled = enabled;
        this.role = role;
        this.last_password_reset_date = last_password_reset_date;
        this.notifications = notifications;
        this.geofences_registry = geofences_registry;
    }

    @Id
    @GeneratedValue
    @Column(name = "ID", unique = true, nullable = false)
    @JsonView({View.NotificationCompleteView.class, View.GeofenceCompleteView.class, View.UserBaseView.class})
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NICK", unique = true, nullable = false, length = 30)
    @JsonView(View.UserBaseView.class)
    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @Column(name = "PASSWORD", nullable = false, length = 60)
    @JsonView(View.UserCompleteView.class)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "FIRST_NAME", nullable = false, length = 30)
    @JsonView({View.GeofenceCompleteView.class, View.UserCompleteView.class})
    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    @Column(name = "LAST_NAME", nullable = false, length = 30)
    @JsonView({View.GeofenceCompleteView.class, View.UserCompleteView.class})
    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    @Column(name = "BIRTHDAY", nullable = false, length = 10)
    @JsonView(View.UserCompleteView.class)
    public java.sql.Date getBirthday() {
        return birthday;
    }

    public void setBirthday(java.sql.Date birthday) {
        this.birthday = birthday;
    }

    @Column(name = "IMEI", nullable = false, length = 15)
    @JsonView(View.UserCompleteView.class)
    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    @JsonView(View.UserCompleteView.class)
    public Set<Geofence> getGeofences() {
        return geofences;
    }

    public void setGeofences(Set<Geofence> geofences) {
        this.geofences = geofences;
    }

    @Column(name = "ENABLED", nullable = false)
    @JsonView(View.UserBaseView.class)
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Column(name = "ROLE", nullable = false, length = 15)
    @JsonView(View.UserBaseView.class)
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Column(name = "LAST_PASSWORD_RESET_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @JsonView(View.UserCompleteView.class)
    public java.util.Date getLastPasswordResetDate() {
        return last_password_reset_date;
    }

    public void setLastPasswordResetDate(java.util.Date last_password_reset_date) {
        this.last_password_reset_date = last_password_reset_date;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    @JsonView(View.UserCompleteView.class)
    public Set<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    public Set<GeofenceRegistry> getGeofencesRegistry() {
        return geofences_registry;
    }

    public void setGeofencesRegistry(Set<GeofenceRegistry> geofences_registry) {
        this.geofences_registry = geofences_registry;
    }

    public String toString() {
        return "User(id: " + id + " nick: " + nick + " password: " + password + " first_name: " + first_name +
                " last_name: " + last_name + " birthday: " + birthday + " imei: " + imei + " geofences: " + geofences +
                " enabled: " + enabled + " role: " + role + " last_password_reset_date: " + last_password_reset_date +
                " notifications: " + notifications + "geofences registry: " + geofences_registry + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                Objects.equals(nick, user.nick) &&
                Objects.equals(password, user.password) &&
                Objects.equals(first_name, user.first_name) &&
                Objects.equals(last_name, user.last_name) &&
                Objects.equals(birthday, user.birthday) &&
                Objects.equals(imei, user.imei) &&
                Objects.equals(geofences, user.geofences) &&
                Objects.equals(enabled, user.enabled) &&
                Objects.equals(role, user.role) &&
                Objects.equals(last_password_reset_date, user.last_password_reset_date) &&
                Objects.equals(notifications, user.notifications) &&
                Objects.equals(geofences_registry, user.geofences_registry);
    }
}